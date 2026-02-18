package titan_scheduler.core;

import java.util.concurrent.PriorityBlockingQueue;

import titan_scheduler.models.ScheduledJob;

public class TitanEngine {
    private final PriorityBlockingQueue<ScheduledJob> jobQueue;
    @SuppressWarnings("unused")
    private final int poolSize;

    public TitanEngine(int poolSize) {// Costruttore
        this.jobQueue = new PriorityBlockingQueue<>();
        this.poolSize = poolSize;
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(jobQueue);
            Thread t = new Thread(worker, "Worker-" + i);
            t.start();
        }
    }
    public void submitJob(ScheduledJob job) {// Aggiunge un job alla queue
        this.jobQueue.add(job);
    }
}