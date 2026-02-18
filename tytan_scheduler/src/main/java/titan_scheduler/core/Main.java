package titan_scheduler.core;

import titan_scheduler.models.ScheduledJob;

public class Main {
    public static void main(String[] args) {
        TitanEngine engine = new TitanEngine(4);

        // Guarda come usiamo il costruttore. 
        // 1 è la priority.
        // () -> { ... } è la nostra Action (il metodo execute al volo)
        engine.submitJob(new ScheduledJob(1, () -> {
            System.out.println("Eseguito task normale da: " + Thread.currentThread().getName());
            return true; // Dobbiamo ritornare un boolean perché l'interfaccia lo richiede
        }));

        engine.submitJob(new ScheduledJob(10, () -> {
            System.out.println("Tentativo di esecuzione task critico...");
            throw new Exception("Errore di connessione al database!");
        }));
        
        engine.submitJob(new ScheduledJob(1, () -> {
            System.out.println("Eseguito task normale da: " + Thread.currentThread().getName());
            return true; // Dobbiamo ritornare un boolean perché l'interfaccia lo richiede
        }));

        engine.submitJob(new ScheduledJob(1, () -> {
            System.out.println("Eseguito task normale da: " + Thread.currentThread().getName());
            return true; // Dobbiamo ritornare un boolean perché l'interfaccia lo richiede
        }));

        engine.submitJob(new ScheduledJob(1, () -> {
            System.out.println("Eseguito task normale da: " + Thread.currentThread().getName());
            return true; // Dobbiamo ritornare un boolean perché l'interfaccia lo richiede
        }));

        System.out.println("Avvio motore...");
    }
}