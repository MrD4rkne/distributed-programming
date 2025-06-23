package cp2024.solution;

import cp2024.circuit.*;
import cp2024.demo.SequentialSolver;
import cp2024.solution.helpers.CircuitNodeGenerator;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CircuitSolverTest {

    @Test
    void runComparision() {
        try {
            testGetValueMultiThreaded(1);
            testGetValueMultiThreaded(2);
            testGetValueMultiThreaded(4);
            testGetValueMultiThreaded(8);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RepeatedTest(10)
    void repeatedPerf() {
        final int LEAFS = 10000;
        final boolean INCLUDE_SLEEPY = false;

        try {
            compareSolvers(LEAFS, INCLUDE_SLEEPY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println();
        System.out.println();
    }

    @Test
    void iteratedPerf() throws InterruptedException {
        final int LEAFS = 10000;
        final boolean INCLUDE_SLEEPY = false;
        CircuitNodeGenerator generator = new CircuitNodeGenerator();

        // Generate a circuit tree
        CircuitNode root = generator.generate(LEAFS, INCLUDE_SLEEPY);
        Circuit circuit = new Circuit(root);

        SequentialSolver sequentialSolver = new SequentialSolver();
        ParallelCircuitSolver parallelSolver = new ParallelCircuitSolver();
        for (int i = 0; i < 10000; i++) {
            // Solve using SequentialSolver
            final long sequentialStart = System.currentTimeMillis();
            CircuitValue sequentialResult = sequentialSolver.solve(circuit);
            boolean sequentialValue = sequentialResult.getValue();
            final long sequentialEnd = System.currentTimeMillis();
            final long sequentialTime = sequentialEnd - sequentialStart;

            // Solve using ParallelSolver
            final long parallelStart = System.currentTimeMillis();

            CircuitValue parallelResult = parallelSolver.solve(circuit);
            boolean parallelValue = parallelResult.getValue();
            final long parallelEnd = System.currentTimeMillis();
            final long parallelTime = parallelEnd - parallelStart;

            // Assert sequential and parallel results are consistent
            assert sequentialValue == parallelValue : "Sequential and parallel results mismatch";

            System.out.println("Leafs: " + LEAFS);
            System.out.println("Sequential Time: " + sequentialTime + "ms");
            System.out.println("Parallel Time: " + parallelTime + "ms");

        }
    }

    public void compareSolvers(int numberOfLeafs, boolean includeSleepy) throws InterruptedException {

        CircuitNodeGenerator generator = new CircuitNodeGenerator();

        // Generate a circuit tree
        CircuitNode root = generator.generate(numberOfLeafs, includeSleepy);
        Circuit circuit = new Circuit(root);

        // Solve using SequentialSolver
        final long sequentialStart = System.currentTimeMillis();
        SequentialSolver sequentialSolver = new SequentialSolver();
        CircuitValue sequentialResult = sequentialSolver.solve(circuit);
        boolean sequentialValue = sequentialResult.getValue();
        final long sequentialEnd = System.currentTimeMillis();
        final long sequentialTime = sequentialEnd - sequentialStart;

        // Solve using ParallelSolver
        final long parallelStart = System.currentTimeMillis();

        ParallelCircuitSolver parallelSolver = new ParallelCircuitSolver();
        CircuitValue parallelResult = parallelSolver.solve(circuit);
        boolean parallelValue = parallelResult.getValue();
        final long parallelEnd = System.currentTimeMillis();
        final long parallelTime = parallelEnd - parallelStart;

        // Assert sequential and parallel results are consistent
        assert sequentialValue == parallelValue : "Sequential and parallel results mismatch";

        System.out.println("Leafs: " + numberOfLeafs);
        System.out.println("Sequential Time: " + sequentialTime + "ms");
        System.out.println("Parallel Time: " + parallelTime + "ms");

        // Stop the parallel solver
        parallelSolver.stop();
    }

    private void testGetValueMultiThreaded(int numThreads) throws InterruptedException {
        CircuitNodeGenerator generator = new CircuitNodeGenerator();

        System.out.println(numThreads);

        final int numberOfLeafs = 1000;
        CircuitNode root = generator.generate(numberOfLeafs, false);
        Circuit circuit = new Circuit(root);

        SequentialSolver sequentialSolver = new SequentialSolver();
        CircuitValue sequentialResult = sequentialSolver.solve(circuit);
        boolean sequentialValue = sequentialResult.getValue();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Boolean>> futures = new ArrayList<>();

        ParallelCircuitSolver parallelSolver = new ParallelCircuitSolver();
        var circuitValue = parallelSolver.solve(circuit);

        for (int i = 0; i < numThreads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    // Introduce random delay to simulate staggered access
                    Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));

                    if (ThreadLocalRandom.current().nextBoolean()) {
                        // Interrupt after some time.
                        new Thread(() -> {
                            try {
                                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }

                    boolean value = circuitValue.getValue();
                    return value;
                } catch (InterruptedException e) {
                    // Ignore interrupted exceptions
                    return null;
                }
            }));
        }

        // Collect and verify results
        for (Future<Boolean> future : futures) {
            try {
                Boolean result = future.get(); // Wait for thread to complete
                if (result != null) {
                    assert result == sequentialValue : "Multithreaded value mismatch";
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Shutting down...");

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        parallelSolver.stop();
    }
}
