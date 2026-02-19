package titan_scheduler.network;

public class MainServer {
    public static void main(String[] args) {
        TitanMaster master = new TitanMaster();
        //master.submitJob(new ScheduledJob(0, "PRINT_CONSOLE", "Test job"));
        //master.submitJob(new ScheduledJob(1, "PRINT_CONSOLE", "Test job"));
        //master.submitJob(new ScheduledJob(2, "PRINT_CONSOLE", "Test job"));
        master.start();
    }
}