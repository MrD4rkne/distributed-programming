package cp2024.solution;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.NodeType;
import cp2024.solution.helpers.CircuitNodeGenerator;
import cp2024.solution.helpers.SequentialSolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Random;

public class BooleanSolverTest {

    @Test
    public void test_and() {
        for (int j = 2; j <= 10; j++) {
            int finalJ = j;
            Assertions.assertDoesNotThrow(
                    () -> test(NodeType.AND, finalJ));
        }
    }

    @Test
    public void test_or() {
        for (int j = 2; j <= 10; j++) {
            int finalJ = j;
            Assertions.assertDoesNotThrow(
                    () -> test(NodeType.OR, finalJ));
        }
    }

    @Test
    public void test_if() {
        Assertions.assertDoesNotThrow(
                () -> test(NodeType.IF, 3));
    }

    @Test
    public void test_not() {
        Assertions.assertDoesNotThrow(
                () -> test(NodeType.NOT, 1));
    }

    @Test
    public void test_gt() {
        for (int j = 2; j <= 10; j++) {
            int finalJ = j;
            for (int threshhold = 0; threshhold <= j + 1; threshhold++) {
                int finalThreshold = threshhold;
                Assertions.assertDoesNotThrow(
                        () -> test(NodeType.GT, finalJ, finalThreshold));
            }
        }
    }

    @Test
    public void test_lt() {
        for (int j = 2; j <= 10; j++) {
            int finalJ = j;
            for (int threshhold = 0; threshhold <= j + 1; threshhold++) {
                int finalThreshold = threshhold;
                Assertions.assertDoesNotThrow(
                        () -> test(NodeType.LT, finalJ, finalThreshold));
            }
        }
    }

    @Test
    void gt_on_to_many_falses_should_return_true() throws InterruptedException {
        Random random = new Random();
        int argsCount = random.nextInt(1000) + 2;
        for (int threshhold = 0; threshhold <= argsCount + 1; threshhold++) {
            CircuitNode[] args = new CircuitNode[argsCount];
            for (int i = 0; i < argsCount; i++) {
                args[i] = CircuitNode.mk(false);
            }
            CircuitNode node = CircuitNode.mk(NodeType.GT, threshhold, args);

            CircuitNodeSolver solver = CircuitSolverFactory.createSolver(node, argsCount);

            // Act & Assert
            // For node to be true it needs 'threshhold + 1' number of true values. So if we
            // put
            // 'argsCount - threshhold' number of false values, it will not be possible.
            for (int i = 0; i < argsCount - threshhold - 1; i++) {
                solver.putResult(new ComputationResult(false, i));
                Assertions.assertFalse(solver.compute().isPresent());
            }

            solver.putResult(new ComputationResult(false, threshhold - argsCount));
            var val = solver.compute();
            Assertions.assertTrue(val.isPresent());
            Assertions.assertFalse(val.get());
        }
    }

    @Test
    void lt_on_to_many_falses_should_return_true() throws InterruptedException {
        Random random = new Random();
        int argsCount = random.nextInt(1000) + 2;
        for (int threshhold = 1; threshhold <= argsCount + 1; threshhold++) {
            CircuitNode[] args = new CircuitNode[argsCount];
            for (int i = 0; i < argsCount; i++) {
                args[i] = CircuitNode.mk(false);
            }
            CircuitNode node = CircuitNode.mk(NodeType.LT, threshhold, args);

            CircuitNodeSolver solver = CircuitSolverFactory.createSolver(node, argsCount);

            // Act & Assert
            // For node to be true it needs 'argsCount - threshold' number of false values.
            // So if we
            // put
            // 'threshhold' number of false values, it will not be possible.
            for (int i = 0; i < argsCount - threshhold; i++) {
                solver.putResult(new ComputationResult(false, i));
                Assertions.assertFalse(solver.compute().isPresent());
            }

            solver.putResult(new ComputationResult(false, threshhold - argsCount));
            var val = solver.compute();
            Assertions.assertTrue(val.isPresent());
            Assertions.assertTrue(val.get());
        }
    }

    @Test
    void lt_0_return_false() throws InterruptedException {
        for (int argsCount = 1; argsCount <= 10; argsCount++) {
            CircuitNode[] args = new CircuitNode[argsCount];
            for (int i = 0; i < argsCount; i++) {
                args[i] = CircuitNode.mk(false);
            }
            CircuitNode node = CircuitNode.mk(NodeType.LT, 0, args);

            CircuitNodeSolver solver = CircuitSolverFactory.createSolver(node, argsCount);

            var val = solver.compute();
            Assertions.assertTrue(val.isPresent());
            Assertions.assertFalse(val.get());
        }
    }

    @Test
    void gt_threshold_bigger_than_argscount_return_false() throws InterruptedException {
        for (int argsCount = 1; argsCount <= 10; argsCount++) {
            CircuitNode[] args = new CircuitNode[argsCount];
            for (int i = 0; i < argsCount; i++) {
                args[i] = CircuitNode.mk(false);
            }

            for (int i = 1; i < 5; i++) {
                CircuitNode node = CircuitNode.mk(NodeType.GT, argsCount + i, args);

                CircuitNodeSolver solver = CircuitSolverFactory.createSolver(node, argsCount);

                var val = solver.compute();
                Assertions.assertTrue(val.isPresent());
                Assertions.assertFalse(val.get());
            }
        }
    }

    @ParameterizedTest()
    @ValueSource(booleans = { true, false })
    @Timeout(1)
    // Tests when in if: 'a ? b : c' value of 'a' is irrelevant, because b==c.
    public void if_forever_condition_and_b_and_c_same_should_return_b(boolean value) throws InterruptedException {
        CircuitNode node = CircuitNode.mk(NodeType.IF,
                CircuitNode.mk(value, Duration.ofSeconds(10)),
                CircuitNode.mk(value),
                CircuitNode.mk(value));
        CircuitNodeSolver solver = CircuitSolverFactory.createSolver(node, node.getArgs().length);

        solver.putResult(new ComputationResult(SequentialSolver.recursiveSolve(node.getArgs()[1]), 1));
        Assertions.assertFalse(solver.compute().isPresent());

        solver.putResult(new ComputationResult(SequentialSolver.recursiveSolve(node.getArgs()[2]), 2));
        var val = solver.compute();
        Assertions.assertTrue(val.isPresent());
        Assertions.assertEquals(value, val.get());
    }

    private void test(NodeType type, int length, int threshold) throws InterruptedException {
        for (int bitmask = 0; bitmask < Math.pow(2, length); bitmask++) {
            CircuitNode[] args = CircuitNodeGenerator.generateCombinations(bitmask, length);

            CircuitNode nodeToSolve = (type == NodeType.GT || type == NodeType.LT)
                    ? CircuitNode.mk(type, threshold, args)
                    : CircuitNode.mk(type, args);

            boolean sequentialValue = SequentialSolver.recursiveSolve(nodeToSolve);
            boolean testedValue = solveFromBlockingQueue(nodeToSolve);

            Assertions.assertEquals(sequentialValue, testedValue);
        }
    }

    private void test(NodeType type, int length) throws InterruptedException {
        test(type, length, 0);
    }

    private boolean solveFromBlockingQueue(CircuitNode node) throws InterruptedException {
        CircuitNodeSolver solver = CircuitSolverFactory.createSolver(node, node.getArgs().length);

        for (int i = 0; i < node.getArgs().length; i++) {
            solver.putResult(new ComputationResult(SequentialSolver.recursiveSolve(node.getArgs()[i]), i));

            var optional = solver.compute();
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        throw new IllegalStateException();
    }
}
