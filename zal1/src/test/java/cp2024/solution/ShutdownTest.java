package cp2024.solution;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.CircuitValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;

public class ShutdownTest {
    @Test
    @Timeout(1)
    public void should_return_value_if_calculated_before_solver_stop() throws InterruptedException {
        // Arrange
        CircuitSolver solver = new ParallelCircuitSolver();
        Circuit c = new Circuit(CircuitNode.mk(true));
        CircuitValue val = solver.solve(c);

        Assertions.assertDoesNotThrow(()->{
            Assertions.assertTrue(val.getValue());
        });

        // Act & Assert

        solver.stop();

        Assertions.assertDoesNotThrow(()->{
            Assertions.assertTrue(val.getValue());
        });
    }

    @Test
    @Timeout(2+1)
    public void should_throw_interrupted_exception_if_solver_stopped_during_computations() throws InterruptedException {
        // Arrange
        CircuitSolver solver = new ParallelCircuitSolver();

        Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(10)));
        CircuitValue val = solver.solve(c);

        new Thread(()-> {
            try {
                Thread.sleep(2000);
                solver.stop();
            } catch (InterruptedException e) {
                Assertions.fail(e);
            }
        }).start();

        // Act & Assert
        final int threadsCount = 10;
        Thread[] threads = new Thread[10];
        for(int i = 0; i<threadsCount; i++) {
            threads[i]=new Thread(() -> {
                Assertions.assertThrows(InterruptedException.class, val::getValue);
            });

            threads[i].start();
        }

        for(int i = 0; i<threadsCount; i++) {
            threads[i].join();
        }
    }

    @Test
    @Timeout(5+1)
    public void should_throw_interrupted_exception_if_solver_stopped_before_calculating_value() {
        // Arrange
        CircuitSolver solver = new ParallelCircuitSolver();
        Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(10)));
        CircuitValue val = solver.solve(c);

        // Act & Assert
        solver.stop();

        Assertions.assertThrows(InterruptedException.class, val::getValue);
    }

    @Test
    @Timeout(2+1)
    public void should_throw_interrupted_when_waiting_thread_interrupted() throws InterruptedException {
        // Arrange
        CircuitSolver solver = new ParallelCircuitSolver();
        Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(2)));
        CircuitValue val = solver.solve(c);

        var threadToInterrupt = new Thread(()->{
            // Even if the first thread was interrupted, the value should be calculated.
            Assertions.assertThrows(InterruptedException.class, val::getValue);
        });

        var peacefulThread = new Thread(()->{
            Assertions.assertDoesNotThrow(()->{
                Assertions.assertTrue(val.getValue());
            });
        });

        // Act
        threadToInterrupt.start();
        peacefulThread.start();
        Thread.sleep(1000);
        threadToInterrupt.interrupt();

        threadToInterrupt.join();
        peacefulThread.join();
    }

    @Test
    @Timeout(5+1)
    public void should_throw_interrupted_if_calling_thread_interrupted_on_getValue() throws InterruptedException{
        // Arrange
        CircuitSolver solver = new ParallelCircuitSolver();
        Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(5)));
        CircuitValue val = solver.solve(c);

        var threadToInterrupt = new Thread(()->{
            Assertions.assertThrows(InterruptedException.class, val::getValue);
        });

        // Act
        threadToInterrupt.interrupt();
        threadToInterrupt.start();

        Thread.sleep(1000);

        Assertions.assertDoesNotThrow(()->{
            // Even if the first thread was interrupted, the value should be calculated.
            Assertions.assertTrue(val.getValue());
        });

        threadToInterrupt.join();
    }
}
