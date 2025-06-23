package cp2024.solution;

import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPool implements WorkersManager {
    private final BlockingDeque<Worker> killedThreads;
    private final Set<Worker> threads;
    private final BlockingDeque<ThreadIdle> idleThreads;
    private final Thread cleanupThread;
    private final ReentrantLock lock = new ReentrantLock();

    private final long timeout;

    private volatile boolean wasShutdown = false;

    public ThreadPool() {
        this(15 * 1000);
    }

    public ThreadPool(long timeout) {
        this.killedThreads = new LinkedBlockingDeque<>();
        this.threads = ConcurrentHashMap.newKeySet();
        this.idleThreads = new LinkedBlockingDeque<>();
        this.timeout = timeout;
        this.cleanupThread = new Thread(this::cleanupIdleThreads);
        this.cleanupThread.start();
    }

    public int getThreadsCount() {
        return threads.size();
    }

    public long getTimeout() {
        return timeout;
    }

    public void shutdown() {
        lock.lock();

        try {
            if (wasShutdown) {
                return;
            }

            wasShutdown = true;
        }finally {
            lock.unlock();
        }

        // Shutdown clean-up.
        cleanupThread.interrupt();

        // Kill all threads
        threads.forEach(Worker::kill);

        try {
            for (Worker worker : killedThreads) {
                worker.join();
            }
            cleanupThread.join();
        } catch (InterruptedException ignored) {
            // According to task description, this will not happen.
        }

        threads.clear();
    }

    @Override
    public boolean isShutdown() {
        return wasShutdown;
    }

    public void submit(Computation computation) throws InterruptedException {
        lock.lock();
        try {
            if (wasShutdown) {
                throw new InterruptedException("The thread pool has been shut down!");
            }

            var idle = idleThreads.poll();
            Worker worker = idle != null ? idle.worker() : null;

            // If no idle worker is available, create a new one
            if (worker == null) {
                worker = new Worker(this);
                threads.add(worker);
                worker.start();
            }

            // Add the task to the worker
            worker.addTask(computation);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void notifyIdle(Worker worker) throws InterruptedException {
        lock.lock();
        try {
            if (wasShutdown) {
                throw new InterruptedException("The thread pool has been shut down!");
            }
        }finally {
            lock.unlock();
        }

        idleThreads.put(new ThreadIdle(worker, System.currentTimeMillis(), timeout));
    }

    private record ThreadIdle(Worker worker, long lastJobTime, long timeout) implements Delayed {
        @Override
        public int compareTo(Delayed o) {
            if (o instanceof ThreadIdle other) {
                return Long.compare(this.getLastJobTime(), other.getLastJobTime());
            }
            throw new IllegalArgumentException("Invalid comparison with non-ThreadIdle object.");
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = (lastJobTime + timeout) - System.currentTimeMillis();
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }

        public long getLastJobTime() {
            return lastJobTime;
        }
    }

    private void cleanupIdleThreads() {
        try {
            do {
                var idle = idleThreads.poll();

                if (idle != null) {
                    long sleep = idle.getDelay(TimeUnit.MILLISECONDS);
                    if (sleep > 0) {
                        idleThreads.putFirst(idle);
                        Thread.sleep(sleep);
                    } else {
                        var worker = idle.worker();
                        worker.kill();
                        threads.remove(worker);
                        killedThreads.add(worker);
                    }
                }

                // Check for shutdown condition
            } while (!Thread.interrupted());
        } catch (InterruptedException ignored) {
            // Clean up thread was interrupted, meaning it should shut down.
            return;
        }
    }
}
