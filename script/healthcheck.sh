#!/bin/sh

LOG_FILE="/logs/healthcheck.log"

echo  "script healthcheck en cours ... " >> "$LOG_FILE"


status=$(curl -s -m 30 http://ms-article:9010/actuator/health | jq -r '.status' )
echo " Résultat de la requête curl : $status" >> "$LOG_FILE"

if [ "$status" = "UP" ]; then
  echo  "success du script de santé : $status" >> "$LOG_FILE"
  exit 0  # Succès
 elif [ "$status" = "DOWN" ]; then
  echo  "Échec du script de santé : $status" >> "$LOG_FILE"
  exit 1  # Échec 1
  else
    echo  "en cours de démarrage : $status" >> "$LOG_FILE"
    exit 0  # Succès
fi