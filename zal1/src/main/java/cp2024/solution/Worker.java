package cp2024.solution;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Worker extends Thread {
    private final BlockingQueue<Computation> tasks;
    private final WorkersManager workersManager;
    private boolean wasKilled = false;

    public Worker(WorkersManager workersManager){
        this.tasks = new LinkedBlockingQueue<>();
        this.workersManager = workersManager;
    }

    public void addTask(Computation computation) throws InterruptedException{
        tasks.add(computation);
    }

    @Override
    public void run(){
        while(true){
            Computation computation = null;
            try {
                computation = tasks.take();
            }catch (InterruptedException ignored){
                break;
            }

            try {
                computation.compute(workersManager);
            }catch (InterruptedException exception){
                computation.cancel();

                if(wasKilled){
                    break;
                }
            }

            try {
                workersManager.notifyIdle(this);
            }catch (InterruptedException ignored){
                // Pool was shut down.
                break;
            }
        }
    }

    public void kill(){
        this.wasKilled=true;
        this.interrupt();
    }
}
