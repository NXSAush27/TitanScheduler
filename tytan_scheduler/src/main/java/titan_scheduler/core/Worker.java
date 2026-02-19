package titan_scheduler.core;

import java.util.concurrent.PriorityBlockingQueue;

import titan_scheduler.models.Action;
import titan_scheduler.models.JobStatus;
import titan_scheduler.models.ScheduledJob;

public class Worker extends Thread {
    private final PriorityBlockingQueue<ScheduledJob> queue;
    private final WalManager walManager;

    public Worker(PriorityBlockingQueue<ScheduledJob> queue, WalManager walManager) {
        this.queue = queue;
        this.walManager = walManager;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Se la coda è vuota, il thread dorme qui (0% CPU). 
                // Può lanciare InterruptedException se spegniamo il motore.
                ScheduledJob job = queue.take(); 
                
                try {
                    // Esecuzione pura
                    Action actionToRun = TaskFactory.createAction(job.getTaskType(), job.getPayload());
                    actionToRun.execute();
                    walManager.logComplete(job.getId());
                    job.setStatus(JobStatus.COMPLETED);
                    
                } catch (Exception e) {
                    // IL TASK È FALLITO! Applichiamo il backoff
                    job.setStatus(JobStatus.FAILED);
                    job.incrementRetryCount();
                    
                    if (job.getRetryCount() > 3) {
                        System.out.println("Job " + job.getId() + " fallito definitivamente: " + e.getMessage());
                    } else {
                        System.out.println("Job fallito, tentativo " + job.getRetryCount() + " in corso...");
                        Thread.sleep((long) Math.pow(2, job.getRetryCount()-1) * 1000); 
                        queue.add(job); // Lo rimette in coda!
                    }
                }
            } catch (InterruptedException ex) {
                // Il sistema sta spegnendo il thread
                System.out.println(Thread.currentThread().getName() + " interrotto.");
                Thread.currentThread().interrupt(); // Buona pratica per mantenere lo stato
                break; // Usciamo dal ciclo infinito
            }
        }
    }
}