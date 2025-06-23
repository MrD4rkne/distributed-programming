package cp2024.solution;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.NodeType;

public class CircuitSolverFactory {
    public static CircuitNodeSolver createSolver(CircuitNode node, int argsCount) {
        if (node.getType() == NodeType.IF) {
            return new IfCircuitNodeSolver(node);
        }

        if (node.getType() == NodeType.LEAF) {
            return new LeafCircuitNodeSolver(node, argsCount);
        }

        if (node.getType() == NodeType.LT) {
            return new LtCircuitNodeSolver(node, argsCount);
        }

        if (node.getType() == NodeType.GT) {
            return new GtCircuitNodeSolver(node, argsCount);
        }

        return new CommonCircuitNodeSolver(node, argsCount);
    }
}
