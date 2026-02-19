package titan_scheduler.network;

import titan_scheduler.models.ScheduledJob;

public class MainServer {
    public static void main(String[] args) {
        TitanMaster master = new TitanMaster();
        master.submitJob(new ScheduledJob(0, "TEST_TASK", "Test job"));
        master.submitJob(new ScheduledJob(1, "TEST_TASK", "Test job"));
        master.submitJob(new ScheduledJob(2, "TEST_TASK", "Test job"));
        master.start();
    }
}