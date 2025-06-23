package cp2024.solution;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitValue;

public class ParallelCircuitValue implements CircuitValue {
    private final ThreadPool solvingService;
    private final HeadComputation computation;

    public ParallelCircuitValue(ThreadPool solvingService, HeadComputation computation) {
        this.solvingService = solvingService;
        this.computation=computation;
    }

    @Override
    public boolean getValue() throws InterruptedException {
        return this.computation.getResult();
    }
}
