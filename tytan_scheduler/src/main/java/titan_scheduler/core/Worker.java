package titan_scheduler.core;

import java.util.concurrent.PriorityBlockingQueue;

import titan_scheduler.models.JobStatus;
import titan_scheduler.models.ScheduledJob;

public class Worker extends Thread {
    private final PriorityBlockingQueue<ScheduledJob> queue;
    
    public Worker(PriorityBlockingQueue<ScheduledJob> queue) {
        this.queue = queue;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ScheduledJob job = new ScheduledJob(MAX_PRIORITY, null);
            try {
                job =  queue.take();
                job.getAction().execute();
            } catch (InterruptedException e) {
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
                queue.add(job);
            }   catch (Exception ex) {
                System.getLogger(Worker.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }
}