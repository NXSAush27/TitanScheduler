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
        String masterHost = System.getenv("MASTER_HOST");
        if (masterHost == null) {
            masterHost = "localhost";
        }

        System.out.println("TitanNode avviato. Cerco il Master su " + masterHost + "...");

        // 1. IL MECCANISMO DI RETRY
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket(masterHost, NetworkProtocol.PORT);
            } catch (Exception e) {
                System.out.println("⏳ Master non ancora in ascolto. Ritento tra 3 secondi...");
                try {
                    Thread.sleep(3000); // Aspetta 3 secondi prima di riprovare
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("✅ Connesso al TitanMaster!");

        // 2. USIAMO IL SOCKET GIA' APERTO NEL TRY-WITH-RESOURCES
        try (Socket activeSocket = socket;
            PrintWriter out = new PrintWriter(activeSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()))) {
            
        // ... qui rimane tutto il resto del codice identico a prima (in.readLine(), ecc.) ...
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
                    
                    // FIX 1: Il parametro -1 forza Java a mantenere gli elementi vuoti finali
                    String[] parts = command.split("\\|", -1); 
                    
                    String jobId = parts[1];
                    String taskType = parts[2];
                    
                    // FIX 2: Programmazione difensiva (Ternary Operator)
                    // Se c'è un payload lo prendiamo, altrimenti stringa vuota
                    String payload = parts.length > 3 ? parts[3] : "";

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