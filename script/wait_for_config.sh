#!/bin/bash

echo  "Lancement du script wait_for_config en cours ... "

if [ -z "$PROFILE_ACTIF_SPRING" ]; then
  PROFILE_ACTIF_SPRING=nas
fi

SERVICE_CONFIG_DOCKER=http://ms-configuration:8089

  while true; do
    response=$(curl -s $SERVICE_CONFIG_DOCKER/msarticle/$PROFILE_ACTIF_SPRING)

    echo "request vers : $SERVICE_CONFIG_DOCKER/msarticle/$PROFILE_ACTIF_SPRING"
    echo "SERVICE_CONFIG_DOCKER : $SERVICE_CONFIG_DOCKER"
    echo "PROFILE_ACTIF_SPRING: $PROFILE_ACTIF_SPRING"

    env

    if [ -n "$response" ]; then
      echo "Le service est en cours d'exécution."
      echo "Lancement du service article ..."
      java -jar app.jar --spring.profiles.active=$PROFILE_ACTIF_SPRING

      break  # Sortir de la boucle si le service est opérationnel
    else
      echo "Le service ms-configuration n'est pas encore opérationnel !"
      sleep 3
    fi

  done
