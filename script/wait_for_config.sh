#!/bin/bash

echo  "Lancement du script wait_for_config en cours ... "

# Dans le cas d'un déployement sur le nas
if [ -z "$PROFILE_ACTIF_SPRING" ]; then

  echo "La variable PROFILE_ACTIF_SPRING => $PROFILE_ACTIF_SPRING <= est absente "
  PROFILE_ACTIF_SPRING=nas
  echo "La variable PROFILE_ACTIF_SPRING est maintenant initialiser => $PROFILE_ACTIF_SPRING | "
  IP=172.17.0.1
  echo "valeur de l'ip de connection a la base de données :  $IP "
fi

echo "Initialisation de l'adresse Ip du service configuration sur le réseau Overlay"
SERVICE_CONFIG_DOCKER=http://ms-configuration:8089
echo "La variable SERVICE_CONFIG_DOCKER est maintenant initialiser => $SERVICE_CONFIG_DOCKER |"

  while true; do
    response=$(curl -s $SERVICE_CONFIG_DOCKER/msarticle/$PROFILE_ACTIF_SPRING)

    echo "request vers : $SERVICE_CONFIG_DOCKER/msarticle/$PROFILE_ACTIF_SPRING"
    echo "SERVICE_CONFIG_DOCKER : $SERVICE_CONFIG_DOCKER"
    echo "PROFILE_ACTIF_SPRING: $PROFILE_ACTIF_SPRING"

    env

    if [ -n "$response" ]; then
      echo "Le service est en cours d'exécution."
      echo "Lancement du service article ..."
      java -jar app.jar --spring.profiles.active=$PROFILE_ACTIF_SPRING -DIP=$IP

      break  # Sortir de la boucle si le service est opérationnel
    else
      echo "Le service ms-configuration n'est pas encore opérationnel !"
      sleep 3
    fi

  done
