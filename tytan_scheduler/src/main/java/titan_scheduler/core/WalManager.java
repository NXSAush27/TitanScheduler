package titan_scheduler.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import titan_scheduler.models.ScheduledJob;

public class WalManager {
    private static final Path WAL_PATH = Paths.get("scheduler.wal");
    private BufferedWriter writer;

    public WalManager() {
        try {
            // Crea il file se non esiste, altrimenti si prepara ad "appendere" (aggiungere in fondo)
            this.writer = Files.newBufferedWriter(WAL_PATH, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile inizializzare il WAL", e);
        }
    }

    // Metodo synchronized: due Worker potrebbero finire contemporaneamente, 
    // non vogliamo che scrivano accavallando i caratteri!
    public synchronized void logSubmit(ScheduledJob job) {
        try {
            String line = "SUBMIT | " + job.getId() + " | " + job.getPriority() + " | " + job.getTaskType() + " | " + job.getPayload();
            writer.write(line);
            writer.newLine(); // Va a capo
            writer.flush();   // FONDAMENTALE: Forza la scrittura fisica su disco ORA.
        } catch (IOException e) {
            System.err.println("Errore critico scrittura WAL: " + e.getMessage());
        }
    }

    public synchronized void logComplete(String jobId) {
        try {
            String line = "COMPLETE | " + jobId;
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Errore critico scrittura WAL: " + e.getMessage());
        }
    }
}