package titan_scheduler.models;

@FunctionalInterface
public interface ActionI {
    // Ritorna true se completato con successo, lancia eccezione se fallisce
    boolean execute() throws Exception;
}