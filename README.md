# TitanScheduler
**A Distributed, Containerized Job Orchestrator built from scratch in Java 21.**

TitanScheduler is a highly concurrent, fault-tolerant job scheduling and execution engine. Built to deeply understand distributed systems without relying on "magic" frameworks like Spring Boot, it handles networking, connection pooling, and multi-threading from the ground up.

![TitanScheduler Dashboard](tytanscheduler/ScreenShot.png)

## Key Features
* **Distributed Master-Worker Architecture:** A central Master node orchestrates tasks and dispatches them to multiple Worker nodes (`TitanNode`) over custom TCP sockets.
* **Concurrency & Thread Safety:** Utilizes Java's `PriorityBlockingQueue` and native Thread management to handle high-throughput job execution.
* **Persistent State & Fault Tolerance:** Jobs are tracked in a PostgreSQL database. If a node crashes, uncompleted jobs (`PENDING` or `RUNNING`) are automatically recovered and reassigned upon restart.
* **Enterprise-grade Database Access:** Implements **HikariCP** for optimal connection pooling and `PreparedStatement` to prevent SQL injection.
* **RESTful API Gateway:** Built with Javalin and Jackson to expose endpoints for external systems to submit and monitor jobs via JSON.
* **Real-time Monitoring Dashboard:** A decoupled, responsive HTML5/TailwindCSS frontend using Vanilla JavaScript (Fetch API) to monitor job execution in real-time.
* **Fully Containerized:** The entire infrastructure (Database, Master, and N scalable Workers) is containerized using **Docker** and orchestrated via **Docker Compose**.

## Architecture Flow

```text
[ Client / Web Dashboard ]
           |
      (HTTP REST / JSON)
           |
           v
  +------------------+                    +------------------+
  |   TitanMaster    |---(JDBC/HikariCP)->|    PostgreSQL    |
  | (API & Scheduler)|<---(Job Recovery)--|   (Persistent)   |
  +------------------+                    +------------------+
           |
   (Custom TCP Protocol)
           |
   +-------+-------+
   |               |
   v               v
+------+       +------+
| Node |       | Node |   <-- Scalable Worker Fleet (TitanNodes)
+------+       +------+

Tech Stack

    Core: Java 21

    Database: PostgreSQL 15, HikariCP, JDBC

    Web/API: Javalin, Jackson (JSON)

    Frontend: HTML5, TailwindCSS, Vanilla JS

    DevOps: Docker, Docker Compose, Bash Scripting, Linux (Ubuntu ARM)

How to Run (Local Environment)
Option 1: Using Docker (Recommended)

You can spin up the entire cluster (Database, Master Server, and Workers) with a single command:
Bash

docker-compose up --build

To scale the number of worker nodes dynamically:
Bash

docker-compose up --scale titan-worker=3

Option 2: Using the Orchestration Script

If you want to run it natively on a Linux machine without Docker (requires Java 21 and Maven):
Bash

chmod +x cluster.sh
./cluster.sh 2   # Starts the Master and 2 Worker nodes

📡 API Endpoints
Method	Endpoint	Description
GET	/api/jobs	Retrieves the list of the last 50 jobs (used by the Dashboard).
POST	/api/jobs	Submits a new job to the cluster.
GET	/api/jobs/{id}	Retrieves the current status of a specific job.

Example POST Request:
Bash

curl -X POST http://localhost:8080/api/jobs \
-H "Content-Type: application/json" \
-d '{"priority": 5, "taskType": "PRINT_CONSOLE", "payload": "Hello from REST API!"}'

🧠 Lessons Learned

Building this project provided deep, hands-on experience with:

    Handling the "Race Condition" problem between Docker containers during startup.

    Managing network communication across boundaries (Docker internal networks vs. Host OS).

    Writing defensive code to prevent ArrayIndexOutOfBoundsException in custom protocol parsing.

    Implementing connection pools to prevent database socket exhaustion.

Developed by [Tuo Nome / Username] - Connect with me on LinkedIn.
