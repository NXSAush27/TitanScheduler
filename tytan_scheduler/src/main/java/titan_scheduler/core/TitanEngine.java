package titan_scheduler.core;

import java.util.PriorityQueue;

import titan_scheduler.models.JobStatus;
import titan_scheduler.models.ScheduledJob;

public class TitanEngine {
    PriorityQueue<ScheduledJob> jobQueue;

    public TitanEngine() {// Costruttore
        this.jobQueue = new PriorityQueue<>();
    }
    public void submitJob(ScheduledJob job) {// Aggiunge un job alla queue
        this.jobQueue.add(job);
    }
    public void run() {// Fa girare il sistema di scheduling
        while (true) {
            ScheduledJob job = jobQueue.poll();
            try{
                job.getAction().execute();
            } catch (Exception e) {
                job.setStatus(JobStatus.FAILED);
                job.incrementRetryCount();
                if (job.getRetryCount() > 3) {
                    System.out.println("Job " + job.getId() + " fallito dopo 3 tentativi.");
                    continue;
                }
                try {
                    Thread.sleep((long) Math.pow(2, job.getRetryCount()-1) * 1000); // Exponential backoff
                } catch (InterruptedException ex) {
                    System.getLogger(TitanEngine.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                jobQueue.add(job);
            }
        }
    }
}