package titan_scheduler.core;

public class Main {
    public static void main(String[] args) {
        TitanEngine engine = new TitanEngine(4);

        // Guarda come usiamo il costruttore. 
        // 1 è la priority.
        // () -> { ... } è la nostra Action (il metodo execute al volo)
        //engine.submitJob(new ScheduledJob(1, "PRINT_CONSOLE", "Task a bassa priorità 1"));
        //engine.submitJob(new ScheduledJob(10, "FAILING_TASK", "Database down!"));
    }
}