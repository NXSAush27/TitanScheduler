package titan_scheduler.models;

import java.util.UUID;

public class ScheduledJob implements Comparable<ScheduledJob> {
    private final String id;
    private final int priority;
    private final String taskType;
    
    private JobStatus status;
    private int retryCount;
    private final String payload;

    public ScheduledJob(int priority, String taskType, String payload) {
        this.id = UUID.randomUUID().toString();
        this.priority = priority;
        this.taskType = taskType;
        this.payload = payload;
        this.status = JobStatus.PENDING;
        this.retryCount = 0;
    }

    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getPayload() {
        return payload;
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