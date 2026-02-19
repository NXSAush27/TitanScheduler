package titan_scheduler.network;

public class NetworkProtocol {
    public static final int PORT = 9999;
    
    // Messaggi dal Worker al Server
    public static final String MSG_READY = "READY"; // "Sono pronto a lavorare"
    public static final String MSG_DONE = "DONE";   // "Ho finito il job"
    public static final String MSG_FAIL = "FAIL";   // "Il job è fallito"

    // Messaggi dal Server al Worker
    public static final String CMD_NO_JOB = "WAIT"; // "Non c'è niente da fare, dormi"
    public static final String CMD_EXECUTE = "EXEC"; // "Esegui questo: ID|TYPE|PAYLOAD"
}