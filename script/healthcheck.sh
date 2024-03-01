#!/bin/sh

logs="/app/logs/healthcheck.log"
exec > "$logs" 2>&1

## Récupérer l'adresse IP de l'interface eth1
IP=$(ifconfig eth1 | awk '/inet / {gsub(/addr:/, "", $2); print $2}')

echo  "script healthcheck en cours ... "
tentative=0
while [ $tentative -lt 5 ]; do

  echo  "Adresse IP service : $IP "
  echo  "requete : http://$IP:9010/actuator/health "

  status=$(curl -s -m 5 http://$IP:9010/actuator/health | jq -r '.status' )
  echo " Résultat de la requête curl : $status"

  if [ "$status" = "UP" ]; then
    echo  "success du script de santé : $status"
    exit 0  # Succès
  elif [ "$status" = "DOWN" ]; then
    echo  "Échec du script de santé : $status"
    exit 1  # Échec 1
  else
    echo  "tentative : $tentative"
    tentative=$((tentative + 1))
    sleep 3
  fi
done