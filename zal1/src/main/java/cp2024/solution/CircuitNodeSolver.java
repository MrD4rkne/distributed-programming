package cp2024.solution;

import cp2024.circuit.*;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class CircuitNodeSolver {
    protected final CircuitNode node;

    protected final BlockingQueue<ComputationResult> resultsQueue;

    protected final Queue<ComputationResult> history;

    public CircuitNodeSolver(CircuitNode node) {
        this.node = node;
        this.resultsQueue = new LinkedBlockingQueue<>();
        this.history= new LinkedBlockingQueue<>();
    }

    public abstract boolean isCalculated();

    public abstract Optional<Boolean> compute() throws InterruptedException;

    public void putResult(ComputationResult result){
        history.add(result);
        resultsQueue.add(result);
    }
}
