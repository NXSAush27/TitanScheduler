package titan_scheduler.models;

@FunctionalInterface
public interface Action {
    // Ritorna true se completato con successo, lancia eccezione se fallisce
    boolean execute() throws Exception;
}