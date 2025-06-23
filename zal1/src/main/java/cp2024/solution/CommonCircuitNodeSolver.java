package cp2024.solution;

import cp2024.circuit.CircuitNode;
import java.util.Optional;

public class CommonCircuitNodeSolver extends CircuitNodeSolver {
    private int trueCount = 0;
    private int computedCount = 0;
    private Optional<Boolean> anwser;
    private final int argsCount;

    public CommonCircuitNodeSolver(CircuitNode node, int argsCount) {
        super(node);

        this.anwser = Optional.empty();
        this.argsCount = argsCount;
    }

    @Override
    public Optional<Boolean> compute() throws InterruptedException {
        if (anwser.isEmpty()) {
            ComputationResult result = resultsQueue.take();
            trueCount += result.val() ? 1 : 0;
            computedCount++;

            switch (node.getType()) {
                case AND -> computeAND();
                case OR -> computeOR();
                case NOT -> computeNOT();
                default -> throw new IllegalArgumentException("Unknown node type");
            }
        }

        return anwser;
    }

    private void computeAND() {
        if (computedCount == argsCount || trueCount != computedCount) {
            anwser = Optional.of(trueCount == computedCount);
        }
    }

    private void computeOR() {
        if (computedCount == argsCount || trueCount > 0) {
            anwser = Optional.of(trueCount > 0);
        }
    }

    private void computeNOT() {
        if (computedCount == 1) {
            anwser = Optional.of(trueCount == 0);
        }
    }

    @Override
    public boolean isCalculated() {
        return anwser.isPresent();
    }
}
