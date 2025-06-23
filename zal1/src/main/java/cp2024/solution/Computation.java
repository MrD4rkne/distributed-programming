package cp2024.solution;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.NodeType;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Computation {
    protected final int id;
    protected final CircuitNode node;
    protected CircuitNodeSolver solver;
    protected ComputationStatus status;
    protected final Computation parent;

    protected final ReentrantLock lock;
    protected final Queue<SubComputation> subComputations;
    private Thread currentThread;
    private int i = 0;

    public Computation(int id, CircuitNode node, Computation parent) {
        this.node = node;
        this.lock = new ReentrantLock();
        this.currentThread = null;
        this.status = ComputationStatus.Created;
        this.subComputations = new LinkedList<>();
        this.id = id;
        this.parent = parent;
    }

    public boolean isFinishedOrCancelled() {
        return status == Computation.ComputationStatus.Done || status == Computation.ComputationStatus.Cancelled;
    }

    public boolean isCancelled() {
        return status == Computation.ComputationStatus.Cancelled;
    }

    public void cancel() {
        lock.lock();

        try {
            if (isFinishedOrCancelled()) {
                return;
            }

            status = Computation.ComputationStatus.Cancelled;
        }finally {
            lock.unlock();
        }

        if (currentThread != null) {
            currentThread.interrupt();
            currentThread = null;
        }

        if (parent != null) {
            parent.cancel();
        }

        synchronized (subComputations) {
            subComputations.forEach(Computation::cancel);
        }
    }

    public synchronized void compute(WorkersManager workersManager) throws InterruptedException {
        lock.lock();

        try {
            if (isFinishedOrCancelled()) {
                throw new InterruptedException();
            }

            currentThread = Thread.currentThread();

            switch (status) {
                case Created -> Initialize(workersManager);
                case InProgress -> TryComputeAnswer(workersManager);
                default -> {
                    return;
                }
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

        }finally {
            currentThread = null;
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void notifyOfResults(ComputationResult result, WorkersManager workersManager) throws InterruptedException {
        solver.putResult(result);
        workersManager.submit(this);
    }

    private void Initialize(WorkersManager workersManager) throws InterruptedException {
        lock.unlock();
        var args = node.getArgs();

        this.solver = CircuitSolverFactory.createSolver(node, args.length);

        synchronized (subComputations) {
            for (int i = 0; i < args.length; i++) {
                SubComputation computation = new SubComputation(i, args[i], this);
                subComputations.add(computation);
                workersManager.submit(computation);
            }
        }

        lock.lock();
        status = Computation.ComputationStatus.InProgress;

        if (solver.isCalculated()) {
            workersManager.submit(this);
        }
    }

    private synchronized void TryComputeAnswer(WorkersManager workersManager) throws InterruptedException {
        i++;
        if(node.getType() == NodeType.LEAF){
            lock.unlock();
        }

        Optional<Boolean> answer = solver.compute();
        if (answer.isEmpty()) {
            if(i == node.getArgs().length){
                throw new IllegalStateException(node.getType().toString());
            }
            return;
        }

        if(node.getType() == NodeType.LEAF){
            lock.lock();
        }

        this.status = Computation.ComputationStatus.Done;

        lock.unlock();

        ComputationResult result = new ComputationResult(answer.get(), id);
        notifyParent(result, workersManager);

        synchronized (subComputations) {
            this.subComputations.forEach(SubComputation::cancel);
        }
    }

    protected abstract void notifyParent(ComputationResult result, WorkersManager workersManager)
            throws InterruptedException;

    protected enum ComputationStatus {
        Created,
        InProgress,
        Done,
        Cancelled
    }
}
