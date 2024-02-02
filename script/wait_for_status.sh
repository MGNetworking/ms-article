#!/bin/bash

  while true; do
    response=$(curl -s http://192.168.1.68:8077/actuator/health)
    status=$(echo $response | jq -r '.status')

    if [ "$status" == "UP" ]; then
      echo "Le service est opérationnel."
      echo "Lancement du service article "
      exec java -jar app.jar
      break  # Sortir de la boucle si le service est opérationnel
    else
      echo "Le service n'est pas encore opérationnel (Statut : $status)"
      sleep 3
    fi

  done
