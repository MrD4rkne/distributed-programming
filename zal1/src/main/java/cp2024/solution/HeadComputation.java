package cp2024.solution;

import cp2024.circuit.CircuitNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeadComputation extends Computation {
    private final BlockingQueue<ComputationResult> results;

    private final List<Thread> waitingThreads;

    private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    public HeadComputation(int id, CircuitNode node) {
        super(id, node, null);
        this.results = new LinkedBlockingQueue<ComputationResult>();
        this.waitingThreads = new ArrayList<>();
    }

    @Override
    public void cancel() {
        super.cancel();

        if(!atomicBoolean.compareAndSet(false,true)){
            return;
        }

        synchronized (waitingThreads) {
            waitingThreads.forEach(Thread::interrupt);
        }
    }

    public boolean getResult() throws InterruptedException {
        lock.lock();
        try {
            if (isCancelled()) {
                throw new InterruptedException();
            }
        }finally {
            lock.unlock();
        }

        synchronized (waitingThreads) {
            waitingThreads.add(Thread.currentThread());
        }

        var result = results.take();
        synchronized (waitingThreads) {
            waitingThreads.remove(Thread.currentThread());
        }

        boolean answer = result.val();
        results.add(result);

        return answer;
    }

    @Override
    protected void notifyParent(ComputationResult result, WorkersManager workersManager) throws InterruptedException {
        results.add(result);
    }
}
