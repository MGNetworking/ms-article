#!/bin/bash

LOG_FILE="/logs/wait_for_config.log"

echo  "Lancement du script wait_for_config en cours ... " >> "$LOG_FILE"

  while true; do
    response=$(curl -s $service_config_docker/msarticle/dev)

    echo "request vers : $service_config_docker/msarticle/dev" >> "$LOG_FILE"
    echo "DEBUG: CONFIG_SERVICE_URI : $CONFIG_SERVICE_URI"
    echo "DEBUG: spring_profiles_active : $spring_profiles_active"

    echo "DEBUG: service_config_docker : $service_config_docker"
    echo "DEBUG: profile_actif_dev : $profile_actif_dev"

    env

    if [ -n "$response" ]; then
      echo "Le service est en cours d'exécution." >> "$LOG_FILE"
      echo "Lancement du service article " >> "$LOG_FILE"
      #exec java -jar app.jar
      java -jar app.jar --spring.profiles.active=$profile_actif_dev

      break  # Sortir de la boucle si le service est opérationnel
    else
      echo "Le service n'est pas encore opérationnel !" >> "$LOG_FILE"
      sleep 3
    fi

  done
