#!/bin/bash

# export des variable d'environnement (pour la compilation via Maven)
export $(cat .env)

env=("Run stack $STACK_NAME" "Compilation / build and Run stack $STACK_NAME" )
echo "Lancement de la compilation (Mode Dev) "
echo "Choisissez votre type de compilation :"

affichage=""
for i in "${!env[@]}"; do
  affichage+="[$i] ${env[$i]} \n"
done

# Affiche a l'utilisateur tout des options disponibles
echo -e "$affichage"
read choix

trouver=false
selection=""

# recherche du choix sélectionné
if [ -n "${env[$choix]}" ]; then
  echo "Vous avez choisi : ${env[$choix]}"
  selection=${env[$choix]}
  trouver=true
fi

run_stack(){

    if [[ -z $(docker images --filter "reference=$DOCKER_IMAGE_NAME"  | grep "$DOCKER_IMAGE_NAME" ) ]]; then

    echo "l'images n'a pas etait trouver vous ne pouvez pas créer la stack"
    exit 1
    fi

    echo "deploy de la stack : $STACK_NAME"
    export PROFILES=dev
    docker stack deploy -c ./docker-compose-swarm.yml $STACK_NAME

    echo "Liste des stack"
    docker service ls

    echo "***************"
    echo "Lancement du service article_ms-article"
    docker service logs -f article_ms-article

}

compilation_Maven(){

    echo "Compilation du projet $STACK_NAME via Maven"

    # Variable d'environnement
    export SERVICE_CONFIG_DOCKER="http://192.168.1.68:8089"
    export PROFILES=dev
    mvn clean package -Dspring.profiles.active=$PROFILES

    echo "Création de l'images : $STACK_NAME"
    docker compose -f docker-compose.yml build --no-cache

    echo "déploiement de la stack du service : $STACK_NAME"
    ./script/deploy.sh

    echo "Liste des stack"
    docker service ls

    echo "***************"
    echo "Lancement du service article_ms-article"
    docker service logs -f article_ms-article
}

docker info >/dev/null 2>&1
DOCKER_STATUS=$?


if [ $DOCKER_STATUS -eq 0 ]; then
  echo "Docker est en cours d'exécution."

  status=$(docker inspect --format='{{.State.Status}}' $STACK_NAME >/dev/null 2>&1)

  # Vérifie l'états du service
  if [[ $status == "running" ]]; then

    timeUTC=$(docker inspect --format='{{.State.StartedAt}}' $STACK_NAME)
    conversion=$(date -d $timeUTC)

    # Si il est toujours en cours d'exécution
    echo "************************************"
    echo "Le conteneur $STACK_NAME est en cour d'exécution depuis : $conversion"
    echo "Suppression du conteneur $STACK_NAME"
    docker compose -f docker-compose.yml logs -f

  elif [[ $status == "exited" ]]; then

    # Si il est toujours en cours d'exécution
    echo "************************************"
    echo "le conteneur $STACK_NAME à été stoppé, mais et toujours actif"
    echo "Suppression du conteneur $STACK_NAME"
    docker container start $STACK_NAME

  else
      if [ $choix -eq 0 ]; then
        run_stack

      elif [ $choix -eq 1  ]; then
        compilation_Maven
      fi
  fi

else
  echo "Docker n'est pas en cours d'exécution."
fi
