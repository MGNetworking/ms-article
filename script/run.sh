#!/bin/bash

$name_conteneur="article"

status=$(docker inspect --format='{{.State.Status}}' $name_conteneur)

if [[ -z $status ]]; then
  echo "Création de l'images est du conteneur $name_conteneur"
  docker compose -f docker-compose-DEV.yml build --no-cache
  docker compose -f docker-compose-DEV.yml up -d
  docker compose -f docker-compose-DEV.yml logs -f

elif [[ $status == "running" ]]; then

  timeUTC=$(docker inspect --format='{{.State.StartedAt}}' $name_conteneur)
  conversion=$(date -d $timeUTC)
  echo "Le conteneur $name_conteneur est en cour d'exécution epuis : $conversion"
  docker compose -f docker-compose-DEV.yml logs -f

elif [[ $status == "exited" ]]; then
  echo "Lancement du conteneur $name_conteneur"
  docker container start $name_conteneur

fi
