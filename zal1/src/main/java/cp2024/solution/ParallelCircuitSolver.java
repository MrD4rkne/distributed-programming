package cp2024.solution;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.CircuitValue;

public class ParallelCircuitSolver implements CircuitSolver {
    private final ThreadPool solvingService;

    public ParallelCircuitSolver() {
        solvingService = new ThreadPool();
    }

    @Override
    public CircuitValue solve(Circuit c) {
        HeadComputation computation = new HeadComputation(0, c.getRoot());
        try {
            solvingService.submit(computation);
        } catch (InterruptedException ignored) {
            // solver was stopped
            computation.cancel();
        }
        return new ParallelCircuitValue(solvingService, computation);
    }

    @Override
    public void stop() {
        solvingService.shutdown();
    }
}
