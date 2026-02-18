package titan_scheduler.core;
import titan_scheduler.models.Action;

public class TaskFactory {
    public static Action createAction(String taskType, String payload) {
        if ("PRINT_CONSOLE".equals(taskType)) {
            return () -> {
                System.out.println("Azione: stampo payload -> " + payload);
                return true;
            };
        } else if ("FAILING_TASK".equals(taskType)) {
            return () -> {
                throw new Exception("Errore simulato su: " + payload);
            };
        }
        throw new IllegalArgumentException("Task sconosciuto: " + taskType);
    }
}