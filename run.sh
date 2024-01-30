#!/bin/bash

name_conteneur="article"

docker info >/dev/null 2>&1
DOCKER_STATUS=$?

if [ $DOCKER_STATUS -eq 0 ]; then
  echo "Docker est en cours d'exécution."

  status=$(docker inspect --format='{{.State.Status}}' $name_conteneur >/dev/null 2>&1)

  # si déjà encours d'ex
  if [[ $status == "running" ]]; then

    timeUTC=$(docker inspect --format='{{.State.StartedAt}}' $name_conteneur)
    conversion=$(date -d $timeUTC)
    echo "************************************"
    echo "Le conteneur $name_conteneur est en cour d'exécution depuis : $conversion"
    docker compose -f ./docker/docker-compose-DEV.yml logs -f

    # si a l'arrêt
  elif [[ $status == "exited" ]]; then
    echo "************************************"
    echo "Lancement du conteneur $name_conteneur"
    docker container start $name_conteneur

  else
    # si n'est pas encor créer
    echo "************************************"
    ./script/Get_IP_Config_Service.sh
    ip_config_service=$?

    ./script/Get_IP_Eureka_Service.sh
    ip_eureka_service=$?

    ./script/version.sh

    if [[ $ip_config_service -eq 0 && $ip_eureka_service -eq 0 ]]; then # 0 = true
      echo "Création de l'images est du conteneur $name_conteneur"
      docker compose -f ./docker/docker-compose-DEV.yml build --no-cache
      docker compose -f ./docker/docker-compose-DEV.yml up -d
      docker compose -f ./docker/docker-compose-DEV.yml logs -f
    else
      echo "Le conteneur $name_conteneur ne sera pas lancer"
    fi
  fi

else
  echo "Docker n'est pas en cours d'exécution."
fi
