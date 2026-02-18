package titan_scheduler.models;

import java.util.UUID;

public class ScheduledJob implements Comparable<ScheduledJob> {
    private final String id;
    private final int priority;
    private final Action action; 
    
    private JobStatus status;
    private int retryCount;

    public ScheduledJob(int priority, Action action) {
        this.id = UUID.randomUUID().toString();
        this.priority = priority;
        this.action = action;
        this.status = JobStatus.PENDING;
        this.retryCount = 0;
    }

    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public Action getAction() {
        return action;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    @Override
    public int compareTo(ScheduledJob other) {
        // (priorità 10 viene prima di priorità 1)
        return Integer.compare(other.priority, this.priority);
    }
}