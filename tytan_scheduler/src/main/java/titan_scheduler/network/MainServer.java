package titan_scheduler.network;

import titan_scheduler.core.DatabaseManager;

public class MainServer {
    public static void main(String[] args) {
        System.out.println("Provo a connettermi a PostgreSQL...");
        DatabaseManager db = new DatabaseManager();
        db.insertJob(new titan_scheduler.models.ScheduledJob(1, "PRINT_CONSOLE", "test-payload"));
        System.out.println("Connessione riuscita!");
    }
}