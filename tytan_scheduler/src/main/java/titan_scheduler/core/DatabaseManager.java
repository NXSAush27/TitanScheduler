package titan_scheduler.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import titan_scheduler.models.JobStatus;
import titan_scheduler.models.ScheduledJob;


public class DatabaseManager {
    private HikariDataSource dataSource;

    public DatabaseManager() {
        HikariConfig config = new HikariConfig();
        
        String dbHost = System.getenv("DB_HOST");
        if (dbHost == null) {
            dbHost = "localhost"; // Fallback per quando lo avvii senza Docker
        }
        config.setJdbcUrl("jdbc:postgresql://" + dbHost + ":5432/titanscheduler");
        config.setUsername("titan");
        config.setPassword("titan123");
        
        // 2. OTTIMIZZAZIONI HIKARICP (Il vero valore enterprise)
        config.setMaximumPoolSize(10); // Massimo 10 connessioni simultanee al DB
        config.setMinimumIdle(2);      // Tieni sempre 2 connessioni pronte in RAM
        config.setConnectionTimeout(30000); // Timeout di 30 secondi

        this.dataSource = new HikariDataSource(config);
        
        // 3. Inizializza la tabella appena il sistema parte
        initDatabase();
    }

    private void initDatabase() {
        // Usiamo i Text Blocks di Java (le tre virgolette) per scrivere SQL leggibile
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS scheduled_jobs (
                id VARCHAR(36) PRIMARY KEY,
                priority INT NOT NULL,
                task_type VARCHAR(50) NOT NULL,
                payload TEXT,
                status VARCHAR(20) NOT NULL,
                retry_count INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        // Usiamo il "try-with-resources" per assicurarci che la Connection torni al Pool!
        try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()) {
            
            stmt.execute(createTableSQL);
            System.out.println("Database inizializzato (Tabella verificata/creata).");
            
        } catch (SQLException e) {
            System.err.println("Errore critico DB: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Metodo che i nostri Worker useranno per farsi dare una connessione
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    

    // 1. Inserisce un nuovo job nel DB
    public void insertJob(ScheduledJob job) {
        String sql = "INSERT INTO scheduled_jobs (id, priority, task_type, payload, status, retry_count) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Sostituiamo i punti interrogativi (?) con i veri valori
            pstmt.setString(1, job.getId());
            pstmt.setInt(2, job.getPriority());
            pstmt.setString(3, job.getTaskType());
            pstmt.setString(4, job.getPayload());
            pstmt.setString(5, job.getStatus().name());
            pstmt.setInt(6, job.getRetryCount());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Errore inserimento job: " + e.getMessage());
        }
    }

    // 2. Aggiorna lo stato di un job esistente (es. quando diventa COMPLETED o FAILED)
    public void updateJobStatus(String jobId, JobStatus status, int retryCount) {
        
        String sql = "UPDATE scheduled_jobs SET status = ?, retry_count = ? WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setInt(2, retryCount);
            pstmt.setString(3, jobId);
            // pstmt.setString(1, ...);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento job: " + e.getMessage());
        }
    }

    // 3. Recupera i job interrotti al riavvio (L'equivalente del vecchio recover() del WAL)
    public List<ScheduledJob> recoverPendingJobs() {
        List<ScheduledJob> recovered = new ArrayList<>();
        // Vogliamo solo i job che non sono né completati né falliti definitivamente
        String sql = "SELECT * FROM scheduled_jobs WHERE status IN ('PENDING', 'RUNNING')";
        
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            
            // Scorriamo i risultati della query riga per riga
            while (rs.next()) {
                String id = rs.getString("id");
                int priority = rs.getInt("priority");
                String taskType = rs.getString("task_type");
                String payload = rs.getString("payload");
                
                ScheduledJob job = new ScheduledJob(id, priority, taskType, payload);
                // NOTA: Dovremmo anche ripristinare il retry_count esatto leggendolo da rs.getInt("retry_count"), 
                // ma per ora il costruttore lo mette a 0, che come policy di riavvio va bene.
                
                recovered.add(job);
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recovery: " + e.getMessage());
        }
        
        System.out.println("Recovery dal DB completato. Job da riprendere: " + recovered.size());
        return recovered;
    }
    public ScheduledJob getJobById(String id) {
        String sql = "SELECT * FROM scheduled_jobs WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int priority = rs.getInt("priority");
                    String taskType = rs.getString("task_type");
                    String payload = rs.getString("payload");
                    ScheduledJob job = new ScheduledJob(id, priority, taskType, payload);

                    // Leggi lo stato reale dal DB e sovrascrivi il default
                    String realStatus = rs.getString("status");
                    job.setStatus(JobStatus.valueOf(realStatus)); 

                    return job;
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero job: " + e.getMessage());
        }
        return null;
    }
    public List<ScheduledJob> getAllJobs() {
        List<ScheduledJob> jobs = new ArrayList<>();
        String sql = "SELECT * FROM scheduled_jobs ORDER BY created_at DESC LIMIT 50";
        
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                ScheduledJob job = new ScheduledJob(
                    rs.getString("id"), 
                    rs.getInt("priority"), 
                    rs.getString("task_type"), 
                    rs.getString("payload")
                );
                job.setStatus(JobStatus.valueOf(rs.getString("status")));
                // job.setRetryCount(rs.getInt("retry_count")); // Se hai il setter
                jobs.add(job);
            }
        } catch (SQLException e) {
            System.err.println("Errore caricamento lista job: " + e.getMessage());
        }
        return jobs;
    }
}