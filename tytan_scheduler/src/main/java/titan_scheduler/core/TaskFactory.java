package titan_scheduler.core;
import titan_scheduler.models.Action;

public class TaskFactory {
    public static Action createAction(String taskType, String payload) {
        //Task minimo 15
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
        else if("SLEEP_TASK".equals(taskType)) {
            return () -> {
                try {
                    Thread.sleep(5000); // Sleep for 5 seconds
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Azione: ho dormito per 5 secondi con payload -> " + payload);
                return true;
            };
        }
        else if("WAIT_TASK".equals(taskType)) {
            return () -> {
                try {
                    Thread.sleep(10000); // Sleep for 10 seconds
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Azione: ho dormito per 10 secondi con payload -> " + payload);
                return true;
            };
        }
        else if("EMPTY_TASK".equals(taskType)) {
            return () -> {
                System.out.println("Azione: task vuoto con payload -> " + payload);
                return true;
            };
        }
        else if("ARRAY_TASK".equals(taskType)) {
            return () -> {
                System.out.println("Azione: task array con payload -> " + payload);
                return true;
            };
        }
        else if("ARITMETIC_TASK".equals(taskType)) {
            return () -> {
                System.out.println("Azione: task aritmetico con payload -> " + payload);
                return true;
            };
        }
        throw new IllegalArgumentException("Task sconosciuto: " + taskType);
    }
}