package titan_scheduler.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList; // Ricorda gli import
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public synchronized void logFail(String jobId) {
        try {
            String line = "FAIL | " + jobId;
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Errore critico scrittura WAL: " + e.getMessage());
        }
    }

    public List<ScheduledJob> recover() {
        if (!Files.exists(WAL_PATH)) {
            return new ArrayList<>();
        }

        Map<String, ScheduledJob> reconstruction = new HashMap<>();

        try {
            List<String> lines = Files.readAllLines(WAL_PATH);
            
            for (String line : lines) {
                // Splittiamo la riga usando il separatore " | "
                String[] parts = line.split(" \\| "); 
                String eventType = parts[0];

                if ("SUBMIT".equals(eventType)) {
                    String id = parts[1];
                    int priority = Integer.parseInt(parts[2]);
                    String taskType = parts[3];
                    String payload = parts[4];
                    
                    // Usiamo il nuovo costruttore che accetta l'ID
                    ScheduledJob job = new ScheduledJob(id, priority, taskType, payload);
                    reconstruction.put(id, job);
                    
                } else if ("COMPLETE".equals(eventType) || "FAIL".equals(eventType)) { 
                    // Se è completato O fallito definitivamente, lo rimuoviamo dalla memoria.
                    String id = parts[1];
                    reconstruction.remove(id); 
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il recovery del WAL", e);
        }

        System.out.println("Recovery completato. Job ripristinati: " + reconstruction.size());
        return new ArrayList<>(reconstruction.values());
    }
}