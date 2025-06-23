package cp2024.solution;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.NodeType;

import java.util.Optional;

public class IfCircuitNodeSolver extends CircuitNodeSolver {
    private final int[] values;
    private Optional<Boolean> anwser;

    public IfCircuitNodeSolver(CircuitNode node) {
        super(node);

        if (node.getType() != NodeType.IF) {
            throw new IllegalArgumentException("This solver solves only ifs");
        }

        this.anwser = Optional.empty();
        this.values = new int[] { -1, -1, -1 };
    }

    @Override
    public Optional<Boolean> compute() throws InterruptedException {
        ComputationResult result = resultsQueue.take();
        if(values[result.i()] != -1){
            throw new IllegalStateException("It was overwritten");
        }

        values[result.i()] = (result.val() ? 1 : 0);

        if (anwser.isEmpty()) {
            tryComputeAnswer();
        }

        return anwser;
    }

    @Override
    public boolean isCalculated() {
        return anwser.isPresent();
    }

    private void tryComputeAnswer() {
        final boolean isConditionIrrelevant = (values[1] != -1 && values[1] == values[2]);
        if (values[0] == -1 && !isConditionIrrelevant) {
            return;
        }

        if (isConditionIrrelevant) {
            anwser = Optional.of(values[1] == 1);
            return;
        }

        if (values[0] == 1) {
            if (values[1] == -1) {
                return;
            }

            anwser = Optional.of(values[1] == 1);
            return;
        }

        if (values[2] == -1) {
            return;
        }

        anwser = Optional.of(values[2] == 1);
    }
}
