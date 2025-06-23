package cp2024.solution;

public interface WorkersManager {

    void shutdown();

    boolean isShutdown();

    void submit(Computation computation) throws InterruptedException;

    void notifyIdle(Worker worker) throws InterruptedException;
}