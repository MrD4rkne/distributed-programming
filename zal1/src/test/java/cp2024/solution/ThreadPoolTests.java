package cp2024.solution;

import cp2024.circuit.CircuitNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadPoolTests {

    private ThreadPool threadPool;

    @AfterEach
    public void tearDown() {
        // Shutdown the thread pool after the test
        threadPool.shutdown();
    }

    @Test
    public void testIdleThreadsAreKilled() throws InterruptedException {
        threadPool = new ThreadPool(10);
        // When a task is submitted, ensure that no actual computation is needed
        // We mock the run method of Computation
        // Submit a mock task to the thread pool
        threadPool.submit(idleComputation());

        // Wait for the task to complete and worker to become idle
        Thread.sleep(1000);

        // Wait longer than the TIME_AFTER_THREAD_IDLE_MS to ensure the cleanup thread
        // can act
        Thread.sleep(TimeUnit.MILLISECONDS.toMillis(2));

        // After the idle time, the worker should be removed (no active threads)
        assertEquals(0, threadPool.getThreadsCount(), "Idle threads were not killed as expected.");
    }

    @Test
    public void testWorkerReusedIfIdle() throws InterruptedException {
        threadPool = new ThreadPool(10_000);
        // Submit a task to the thread pool
        threadPool.submit(idleComputation());

        // Wait for the task to complete and the worker to become idle
        Thread.sleep(100);

        // Submit another task and check if the same worker is reused
        threadPool.submit(idleComputation());

        // Check the number of threads in the pool - it should still be 1 (reused)
        assertEquals(1, threadPool.getThreadsCount(), "Worker should be reused.");
    }

    private Computation idleComputation() {
        return new Computation(0, CircuitNode.mk(false), null) {
            @Override
            protected void notifyParent(ComputationResult result, WorkersManager workersManager)
                    throws InterruptedException {

            }

            @Override
            public void compute(WorkersManager workersManager) throws InterruptedException {
            }
        };
    }
}