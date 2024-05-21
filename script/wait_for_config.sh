#!/bin/bash

echo  "Lancement du script wait_for_config en cours ... "

# Dans le cas d'un déployement sur le nas
if [ -z "$PROFILE_ACTIF_SPRING" ] || [ -z "$SERVICE_CONFIG_DOCKER" ] || [ -z "$IP"  ]; then

  echo  "Les variables de configuration sont absente, initialisation Nas lancer"

  echo "La variable PROFILE_ACTIF_SPRING => $PROFILE_ACTIF_SPRING <="
  echo "L'adresse IP du service configuration sur le réseau Overlay: $SERVICE_CONFIG_DOCKER <="
  echo "valeur de l'adresse IP de connection à la base de données  $IP <="

  PROFILE_ACTIF_SPRING=nas
  IP=172.17.0.1
  SERVICE_CONFIG_DOCKER=http://ms-configuration:8089

  echo  "Les variables de configuration sont initialiser"
  echo "La variable PROFILE_ACTIF_SPRING => $PROFILE_ACTIF_SPRING <="
  echo "L'adresse IP du service configuration sur le réseau Overlay: $SERVICE_CONFIG_DOCKER <="
  echo "valeur de l'adresse IP de connection à la base de données  $IP <="
fi


  while true; do
    response=$(curl -s $SERVICE_CONFIG_DOCKER/msarticle/$PROFILE_ACTIF_SPRING)

    echo "request vers : $SERVICE_CONFIG_DOCKER/msarticle/$PROFILE_ACTIF_SPRING"

    echo "**********************************"
    echo "Liste des variables d'environnment"
    env
    echo "**********************************"

    if [ -n "$response" ]; then
      echo "Le service ms-configuration est en cours d'exécution."
      echo "Lancement du service ms-article ..."
      java -jar app.jar --spring.profiles.active=$PROFILE_ACTIF_SPRING --IP=$IP

      break  # Sortir de la boucle si le service est opérationnel
    else
      echo "Le service ms-configuration n'est pas encore opérationnel !"
      sleep 3
    fi

  done
