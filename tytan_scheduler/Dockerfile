# Usiamo una versione leggera di Linux (Alpine) con Java 21 preinstallato
FROM eclipse-temurin:21-jre-alpine

# Creiamo una cartella di lavoro
WORKDIR /app

# Copiamo il nostro Fat JAR dentro il container
COPY target/tytan_scheduler-1.0-jar-with-dependencies.jar /app/titan.jar

# Il comando di avvio lo decideremo nel docker-compose, 
# dato che la stessa immagine farà sia da Master che da Worker!