package titan_scheduler.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.PriorityBlockingQueue;

import titan_scheduler.core.WalManager;
import titan_scheduler.models.ScheduledJob;

public class TitanMaster {
    private PriorityBlockingQueue<ScheduledJob> queue;
    private WalManager walManager;
    private boolean running = true;

    public TitanMaster() {
        // Inizializza coda e WAL (col recovery!) come facevi nel vecchio TitanEngine
        this.queue = new PriorityBlockingQueue<>();
        this.walManager = new WalManager();
        var recovered = walManager.recover();
        this.queue.addAll(recovered);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(NetworkProtocol.PORT)) {
            System.out.println("TitanMaster in ascolto sulla porta " + NetworkProtocol.PORT);

            while (running) {
                // Questo metodo è BLOCCANTE: aspetta finché un worker non si collega
                Socket clientSocket = serverSocket.accept(); 
                System.out.println("Nuovo worker connesso: " + clientSocket.getInetAddress());

                // Per ogni worker, lanciamo un thread dedicato che gestisce la comunicazione
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Questa classe interna gestisce UN singolo worker
    // In titan_scheduler/network/TitanMaster.java

    private class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                
                String message;
                // FIX: Aggiunto ciclo while per mantenere viva la conversazione
                while ((message = in.readLine()) != null) {
                    
                    if(message.startsWith(NetworkProtocol.MSG_READY)) {
                        ScheduledJob job = queue.poll();
                        if(job != null) {
                            out.println(NetworkProtocol.CMD_EXECUTE + "|" + job.getId() + "|" + job.getTaskType() + "|" + job.getPayload());
                        } else {
                            out.println(NetworkProtocol.CMD_NO_JOB);
                        }
                    } else if(message.startsWith(NetworkProtocol.MSG_DONE)) {
                        String[] parts = message.split("\\|");
                        if(parts.length >= 2) {
                            walManager.logComplete(parts[1]);
                            System.out.println("Job completato: " + parts[1]); // Log utile lato server
                        }
                    } else if(message.startsWith(NetworkProtocol.MSG_FAIL)) {
                        String[] parts = message.split("\\|");
                        if(parts.length >= 2) {
                            walManager.logFail(parts[1]);
                            System.out.println("Job fallito: " + parts[1]);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Worker disconnesso.");
            } finally {
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }
    
    // Metodo per permettere al Main di aggiungere job (altrimenti la coda resta vuota!)
    public void submitJob(ScheduledJob job) {
        walManager.logSubmit(job);
        queue.add(job);
    }
}