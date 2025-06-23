package cp2024.solution;

import cp2024.circuit.CircuitNode;

public class SubComputation extends Computation {

    public SubComputation(int id, CircuitNode node, Computation parent) {
        super(id, node, parent);
    }

    @Override
    protected void notifyParent(ComputationResult result, WorkersManager workersManager) throws InterruptedException {
        parent.notifyOfResults(result,workersManager);
    }
}
