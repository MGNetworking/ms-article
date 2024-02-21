#!/bin/bash

echo  "Lancement du script wait_for_config en cours ... "

  while true; do
    response=$(curl -s $service_config_docker/msarticle/dev)

    echo "request vers : $service_config_docker/msarticle/dev"
    echo "DEBUG: CONFIG_SERVICE_URI : $CONFIG_SERVICE_URI"
    echo "DEBUG: spring_profiles_active : $spring_profiles_active"

    echo "DEBUG: service_config_docker : $service_config_docker"
    echo "DEBUG: profile_actif_dev : $profile_actif_dev"

    env

    if [ -n "$response" ]; then
      echo "Le service est en cours d'exécution."
      echo "Lancement du service article "
      #exec java -jar app.jar
      java -jar app.jar --spring.profiles.active=$profile_actif_dev

      break  # Sortir de la boucle si le service est opérationnel
    else
      echo "Le service n'est pas encore opérationnel !"
      sleep 3
    fi

  done
