#!/bin/bash

handle_error() {
  echo "Une erreur et survenu lors de l'exécution de la commande : $1"
  echo "Fin du script"
  exit 1
}

run_stack(){

    if [[ -z $(docker images --filter "reference=$DOCKER_IMAGE_NAME"  | grep "$DOCKER_IMAGE_NAME" ) ]]; then

      echo "L'images docker est absente, vous ne pouvez pas créer la stack sans image docker !!!"

      else
        echo "deploy de la stack : $STACK_NAME"
        export IMAGE_VERSION="$DOCKER_IMAGE_NAME:$IMAGE_VERSION-beta"
        ./script/deploy.sh beta || handle_error "Exécution du script de Déploiement en version $version_beta"
    fi
}

compilation_Maven(){

    echo "Compilation du projet $STACK_NAME via Maven"
    mvn clean package "-Dspring.profiles.active="$PROFILES "-DEUREKA_DEFAULTZONE="http://localhost:8099/eureka

    echo "Création de l'images : $STACK_NAME"
    docker compose -f docker-compose.yml build --no-cache || handle_error "Construction de l'image Docker"

    echo "---------------------------"
    echo "tage de l'image $DOCKER_IMAGE_NAME:$IMAGE_VERSION vers $version_beta"
    docker tag "$DOCKER_IMAGE_NAME:$IMAGE_VERSION" "$version_beta" || handle_error "Tag de l'image Docker"

    echo "---------------------------"
    echo "deploiement de la stack: $STACK_NAME en version beta"
    ./script/deploy.sh beta $PROFILES || handle_error "Exécution du script de Déploiement en version $version_beta"

}

status(){

  echo "docker service ls"
  docker service ls

  echo "---------------------------"
  echo "docker service ps $NAME_SERVICE "
  docker service ps $NAME_SERVICE
}

docker info >/dev/null 2>&1
DOCKER_STATUS=$?

if [ $DOCKER_STATUS -ne 0 ]; then
  echo "Docker n'est pas en cours d'exécution, sorti : " $DOCKER_STATUS
  exit 1
fi


echo "---------------------------"
export $(cat .env)
export PROFILES=devDocker
version_beta="$DOCKER_IMAGE_NAME:$IMAGE_VERSION-beta"

while true; do
  env=("Exit" "Run stack" "Build and Run stack" "Down stack" "status stack" )
  echo "Lancement de la compilation (Mode Dev) "
  echo "Choisissez votre type de compilation :"

  affichage=""
  for i in "${!env[@]}"; do
    affichage+="[$i] ${env[$i]} \n"
  done

  # Affiche a l'utilisateur tout des options disponibles
  echo -e "$affichage"
  read choix

  # recherche du choix sélectionné
  if [ -n "${env[$choix]}" ]; then
    echo "-------------[ ${env[$choix]} ]--------------"

  fi

  status=$(docker inspect --format='{{.State.Status}}' $STACK_NAME >/dev/null 2>&1)

  # Si le conteneur est déjà en cours d'exécution
  if [[ $status == "running" ]]; then

    timeUTC=$(docker inspect --format='{{.State.StartedAt}}' $STACK_NAME)
    conversion=$(date -d $timeUTC)

    echo "---------------------------"
    echo "Le conteneur $STACK_NAME est en cour d'exécution depuis : $conversion"
    echo "Suppression du conteneur $STACK_NAME"
    docker compose -f docker-compose.yml logs -f || handle_error "Affichage des logs du conteneur $STACK_NAME"

  elif [[ $status == "exited" ]]; then

    echo "---------------------------"
    echo "le conteneur $STACK_NAME à été stoppé, mais et toujours actif"
    echo "Suppression du conteneur $STACK_NAME"
    docker container start $STACK_NAME || handle_error "Redémarrage du conteneur $STACK_NAME"

  else

      if [ $choix -eq 0 ]; then
        exit 0
      elif [ $choix -eq 1  ]; then
        run_stack
      elif [ $choix -eq 2  ]; then
        compilation_Maven
      elif [ $choix -eq 3  ]; then
        ./script/down.sh
      elif [ $choix -eq 4  ]; then
        status
      fi
  fi

done