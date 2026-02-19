#!/bin/bash

# Legge il numero di worker passati come argomento (se non specificato, usa 2)
NODES=${1:-2}

echo "Compilazione del progetto e creazione del Fat JAR..."
# "package" innesca il plugin assembly che abbiamo appena aggiunto
mvn clean package -DskipTests

JAR_FILE="target/tytan_scheduler-1.0-jar-with-dependencies.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "Errore durante la compilazione. JAR non trovato!"
    exit 1
fi

echo "Avvio del TitanMaster (Server e UI)..."
# La "&" commerciale lancia il processo in background senza bloccare il terminale
java -cp $JAR_FILE titan_scheduler.network.MainServer &

echo "Attendo 3 secondi per l'avvio del server Web e del DB..."
sleep 3

echo "Avvio di $NODES TitanNode (Worker)..."
for i in $(seq 1 $NODES); do
    java -cp $JAR_FILE titan_scheduler.network.TitanNode &
    echo "   -> Worker $i avviato."
done

echo ""
echo "=================================================="
echo "CLUSTER IN ESECUZIONE!"
echo "Dashboard: http://localhost:8080"
echo "Nodi Worker attivi: $NODES"
echo "=================================================="
echo ""
echo "PREMI INVIO PER SPEGNERE L'INTERO CLUSTER"
read

# Quando premi Invio, questa riga killa (uccide) tutti i processi in background creati da questo script
echo "Spegnimento in corso..."
kill $(jobs -p) 2>/dev/null
echo "Cluster arrestato con successo. Arrivederci!"