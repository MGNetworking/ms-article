#!/bin/bash

  echo "Lancement du script wait_for_config en cours ..."
  echo "DEBUG - PROFILE_ACTIF_SPRING=$PROFILE_ACTIF_SPRING"
  echo "DEBUG - SERVICE_CONFIG_URI=$SERVICE_CONFIG_URI"

# Lancement en mode devDocker. N'a pas besoin dus service config
if [ "$PROFILE_ACTIF_SPRING" = "devDocker" ]; then
      echo "Lancement du service ms-article avec le profile devDocker ..."
      exec java -jar app.jar --spring.profiles.active=$PROFILE_ACTIF_SPRING
else

  while true; do
    response=$(curl -s $SERVICE_CONFIG_URI/msarticle/$PROFILE_ACTIF_SPRING)

    echo "request vers : $SERVICE_CONFIG_DOCKER/msarticle/$PROFILE_ACTIF_SPRING"

    echo "**********************************"
    echo "Liste des variables d'environnement"
    env
    echo "**********************************"

    if [ -n "$response" ]; then
      echo "Le service ms-configuration est en cours d'exécution."
      echo "Lancement du service ms-article ..."
      exec java -jar app.jar --spring.profiles.active=$PROFILE_ACTIF_SPRING

      break  # Sortir de la boucle si le service est opérationnel
    else
      echo "Le service ms-configuration n'est pas encore opérationnel !"
      sleep 3
    fi

  done

fi
