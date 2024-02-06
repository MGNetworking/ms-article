#!/bin/bash

name_conteneur="ms-article"

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
    docker compose logs -f

    # si a l'arrêt
  elif [[ $status == "exited" ]]; then

    echo "************************************"
    echo "Lancement du conteneur $name_conteneur"
    docker container start $name_conteneur

  else
    # si n'est pas encor créer
    echo "************************************"
    echo "Création de l'images est du conteneur $name_conteneur"
    export CONFIG_SERVICE_URI_host="http://192.168.1.68:8089"
    export spring_profiles_active="dev"
    export version="1.0.0"
    DOCKER_DEPOT="sonatype-nexus.backhole.ovh"

    echo "build images $name_conteneur with $CONFIG_SERVICE_URI_host and $spring_profiles_active !!!"
    docker build --no-cache -t $DOCKER_DEPOT/$name_conteneur-service:$version --build-arg CONFIG_SERVICE_URI_host=$CONFIG_SERVICE_URI_host --build-arg SPRING_PROFILES_ACTIVE=$spring_profiles_active .

    echo "Création de la stack du service : $name_conteneur"
    docker stack deploy -c ./docker-compose-swarm.yml stack

  fi

else
  echo "Docker n'est pas en cours d'exécution."
fi
