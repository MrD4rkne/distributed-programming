package cp2024.solution;

import cp2024.circuit.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class DemoTest {
    @Test
    public void demo() throws InterruptedException {
        CircuitSolver solver = new ParallelCircuitSolver();

        Circuit c = new Circuit(CircuitNode.mk(true));
        Assertions.assertTrue(solver.solve(c).getValue());

        c = new Circuit(CircuitNode.mk(false, Duration.ofSeconds(3)));
        CircuitValue firstValue = solver.solve(c);

        c = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false, Duration.ofSeconds(3))));
        CircuitValue secondValue = solver.solve(c);

        Assertions.assertFalse(secondValue.getValue());
        Assertions.assertFalse(firstValue.getValue());

        c = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true, Duration.ofSeconds(3))));
        CircuitValue thirdValue = solver.solve(c);

        Assertions.assertFalse(thirdValue.getValue());
        solver.stop();

        c = new Circuit(CircuitNode.mk(true));

        Circuit finalC = c;
        Assertions.assertThrows(InterruptedException.class, () -> solver.solve(finalC).getValue());

        Assertions.assertDoesNotThrow(thirdValue::getValue);
        Assertions.assertFalse(thirdValue.getValue());
    }
}
