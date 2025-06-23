package cp2024.solution;

import java.util.Optional;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.LeafNode;
import cp2024.circuit.NodeType;

public class LeafCircuitNodeSolver extends CircuitNodeSolver {

    private Optional<Boolean> anwser;

    public LeafCircuitNodeSolver(CircuitNode node, int argsCount) {
        super(node);

        if (node.getType() != NodeType.LEAF) {
            throw new IllegalArgumentException("This solver solves only leafs");
        }

        this.anwser = Optional.empty();
    }

    @Override
    public boolean isCalculated() {
        return true;
    }

    @Override
    public Optional<Boolean> compute() throws InterruptedException {
        if (anwser.isEmpty()) {
            anwser = Optional.of(((LeafNode) node).getValue());
        }

        return anwser;
    }
}
