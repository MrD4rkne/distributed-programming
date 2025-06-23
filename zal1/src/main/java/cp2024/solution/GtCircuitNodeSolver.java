package cp2024.solution;

import java.util.Optional;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.NodeType;
import cp2024.circuit.ThresholdNode;

public class GtCircuitNodeSolver extends CircuitNodeSolver {
    private int trueCount = 0;
    private int computedCount = 0;
    private Optional<Boolean> anwser;
    private final int argsCount;

    public GtCircuitNodeSolver(CircuitNode node, int argsCount) {
        super(node);

        if (node.getType() != NodeType.GT) {
            throw new IllegalArgumentException("This solver solves only gts");
        }

        this.anwser = Optional.empty();
        this.argsCount = argsCount;
    }

    @Override
    public Optional<Boolean> compute() throws InterruptedException {
        if (!isCalculated()) {
            ComputationResult result = resultsQueue.take();
            trueCount += result.val() ? 1 : 0;
            computedCount++;
        }

        if (anwser.isEmpty()) {
            if (trueCount >= ((ThresholdNode) node).getThreshold() + 1
                    || argsCount - computedCount + trueCount < ((ThresholdNode) node).getThreshold() + 1) {
                anwser = Optional.of(trueCount >= ((ThresholdNode) node).getThreshold() + 1);
            }
        }

        return anwser;
    }

    @Override
    public boolean isCalculated() {
        return anwser.isPresent() || ((ThresholdNode) node).getThreshold() > argsCount;
    }
}
