package titan_scheduler.core;

import java.util.concurrent.PriorityBlockingQueue;

import titan_scheduler.models.ScheduledJob;

public class TitanEngine {
    private final PriorityBlockingQueue<ScheduledJob> jobQueue;
    @SuppressWarnings("unused")
    private final int poolSize;
    private final DatabaseManager databaseManager;

    public TitanEngine(int poolSize) {
        this.jobQueue = new PriorityBlockingQueue<>();
        this.poolSize = poolSize;
        this.databaseManager = new DatabaseManager();
        
        // --- FASE DI RECOVERY ---
        // Prima di accettare nuovi job, ricarichiamo quelli vecchi
        var recoveredJobs = this.databaseManager.recoverPendingJobs();
        for (ScheduledJob job : recoveredJobs) {
            this.jobQueue.add(job); // Li rimettiamo in coda!
        }
        // ------------------------

        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(jobQueue, this.databaseManager);
            Thread t = new Thread(worker, "Worker-" + i);
            t.start();
        }
    }
    public void submitJob(ScheduledJob job) {// Aggiunge un job alla queue
        this.databaseManager.insertJob(job);
        this.jobQueue.add(job);
    }
}