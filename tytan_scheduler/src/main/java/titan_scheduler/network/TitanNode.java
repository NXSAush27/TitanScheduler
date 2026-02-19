package titan_scheduler.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import titan_scheduler.core.TaskFactory;
import titan_scheduler.models.Action;

public class TitanNode {
    
    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        System.out.println("TitanNode avviato. Tentativo di connessione al Master...");

        try (Socket socket = new Socket("localhost", NetworkProtocol.PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connesso al Master su " + socket.getInetAddress());

            while (true) {
                // 1. Dico al server che sono libero
                out.println(NetworkProtocol.MSG_READY);

                // 2. Leggo cosa devo fare
                String command = in.readLine();
                
                if (command == null) {
                    System.out.println("Il Server ha chiuso la connessione.");
                    break;
                }

                if (command.equals(NetworkProtocol.CMD_NO_JOB)) {
                    // Nessun lavoro, aspetto un po' prima di richidere
                    System.out.println("Nessun job disponibile. Attendo...");
                    try { Thread.sleep(3000); } catch (InterruptedException e) {}
                
                } else if (command.startsWith(NetworkProtocol.CMD_EXECUTE)) {
                    // Protocollo: EXEC|id|type|payload
                    String[] parts = command.split("\\|");
                    String jobId = parts[1];
                    String taskType = parts[2];
                    String payload = parts[3];
                    System.out.println("jobId: " + jobId);
                    System.out.println("Ricevuto Job: " + jobId + " [" + taskType + "]");

                    try {
                        // Uso la logica Core esistente!
                        Action action = TaskFactory.createAction(taskType, payload);
                        action.execute();
                        
                        // Comunico successo
                        out.println(NetworkProtocol.MSG_DONE + "|" + jobId);
                        System.out.println("Job completato e notificato.");
                        
                    } catch (Exception e) {
                        System.err.println("Errore esecuzione job: " + e.getMessage());
                        // Comunico fallimento
                        out.println(NetworkProtocol.MSG_FAIL + "|" + jobId);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}