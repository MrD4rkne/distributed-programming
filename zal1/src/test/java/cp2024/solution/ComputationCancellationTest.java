package cp2024.solution;

import cp2024.circuit.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;

public class ComputationCancellationTest {

        @Test
        @Timeout(6 + 1)
        public void test_downward_cancellation() throws InterruptedException {

                CircuitNode[] args = new CircuitNode[] {
                                CircuitNode.mk(false),
                                CircuitNode.mk(true, Duration.ofSeconds(5)),
                                CircuitNode.mk(NodeType.AND,
                                                CircuitNode.mk(false, Duration.ofSeconds(100000)),
                                                CircuitNode.mk(true))
                };

                CircuitNode node = CircuitNode.mk(NodeType.OR, args);
                Circuit c = new Circuit(node);

                CircuitSolver circuitSolver = new ParallelCircuitSolver();
                CircuitValue value = circuitSolver.solve(c);

                Assertions.assertTrue(value.getValue());
        }

        @Test
        @Timeout(2 + 1)
        public void test_upwards_cancellation() throws InterruptedException {

                CircuitNode[] args = new CircuitNode[] {
                                CircuitNode.mk(false),
                                CircuitNode.mk(NodeType.AND,
                                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                                CircuitNode.mk(true)),
                                CircuitNode.mk(NodeType.AND,
                                                CircuitNode.mk(false, Duration.ofSeconds(100000)),
                                                CircuitNode.mk(true))
                };

                CircuitNode node = CircuitNode.mk(NodeType.OR, args);
                Circuit c = new Circuit(node);

                CircuitSolver circuitSolver = new ParallelCircuitSolver();
                CircuitValue value = circuitSolver.solve(c);

                Assertions.assertTrue(value.getValue());
        }

        @Test
        @Timeout(1)
        public void test_quick_cancellation() throws InterruptedException {

                CircuitNode[] args = new CircuitNode[] {
                                CircuitNode.mk(false),
                                CircuitNode.mk(NodeType.AND,
                                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                                CircuitNode.mk(true)),
                                CircuitNode.mk(NodeType.AND,
                                                CircuitNode.mk(false, Duration.ofSeconds(100000)),
                                                CircuitNode.mk(true))
                };

                CircuitNode node = CircuitNode.mk(NodeType.AND, args);
                Circuit c = new Circuit(node);

                CircuitSolver circuitSolver = new ParallelCircuitSolver();
                CircuitValue value = circuitSolver.solve(c);

                Assertions.assertFalse(value.getValue());
        }
}
