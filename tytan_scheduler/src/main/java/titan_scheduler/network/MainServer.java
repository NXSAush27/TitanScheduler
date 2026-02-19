package titan_scheduler.network;

import io.javalin.Javalin;
import titan_scheduler.models.ScheduledJob;

public class MainServer {
    public static void main(String[] args) {
        
        // 1. Avvia il nostro motore Master in un thread separato (TCP sulla porta 9999)
        TitanMaster master = new TitanMaster();
        new Thread(() -> master.start()).start();

        // 2. Avvia il server Web RESTful sulla porta 8080
        Javalin app = Javalin.create(config -> {
            // Diciamo a Javalin di cercare i file statici nella cartella "public"
            config.staticFiles.add("/public", io.javalin.http.staticfiles.Location.CLASSPATH);
        }).start(8080);

        // ENDPOINT 1: Riceve un JSON e sottomettte il job
        app.post("/api/jobs", ctx -> {
            // Javalin e Jackson leggono il JSON in arrivo e popolano questo Record automaticamente
            JobRequest request = ctx.bodyAsClass(JobRequest.class);
            
            // Creiamo il vero ScheduledJob (il costruttore genererà l'UUID e lo stato PENDING)
            ScheduledJob job = new ScheduledJob(request.priority(), request.taskType(), request.payload());
            
            // Passiamo il job al motore che scriverà su DB e lo metterà in coda
            master.submitJob(job);
            
            // Ritorniamo 201 (Created) e l'oggetto in formato JSON al client
            ctx.status(201).json(job);
        });

        // ENDPOINT 2: Restituisce lo stato di un job
        app.get("/api/jobs/{id}", ctx -> {
            String jobId = ctx.pathParam("id");
            
            ScheduledJob job = master.getDatabaseManager().getJobById(jobId);
            if (job != null) {
                ctx.json(job);
            } else {
                ctx.status(404).result("Job non trovato");
            }
        });
        // ENDPOINT 3: Restituisce la lista di tutti i job per la dashboard
        app.get("/api/jobs", ctx -> {
            ctx.json(master.getDatabaseManager().getAllJobs());
        });
    }
}

// Un "Data Transfer Object" (DTO) per mappare il JSON in arrivo
record JobRequest(int priority, String taskType, String payload) {}