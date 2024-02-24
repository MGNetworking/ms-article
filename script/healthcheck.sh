#!/bin/sh

echo  "script healthcheck en cours ... "

status=$(curl -s -m 30 http://ms-article:9010/actuator/health | jq -r '.status' )
echo " Résultat de la requête curl : $status"

if [ "$status" = "UP" ]; then
  echo  "success du script de santé : $status"
  exit 0  # Succès
 elif [ "$status" = "DOWN" ]; then
  echo  "Échec du script de santé : $status"
  exit 1  # Échec 1
  else
    echo  "en cours de démarrage : $status"
    exit 0  # Succès
fi