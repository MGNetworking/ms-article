#!/bin/bash

name_conteneur="article"

env=("Compilation via Dockerfile" "Compilation via Maven puis copier des fichiers dans le Dockerfile" )
echo "Lancement du pour le développement en Mode Dev "
echo "Choisissez votre type de compilation :"

affichage=""
for i in "${!env[@]}"; do
  affichage+="[$i] pour l'environnement ${env[$i]} \n"
done

# Affiche a l'utilisateur tout des options disponibles
echo -e "$affichage"
read choix


trouver=false
selection=""
# recherche du choix sélectionné
if [ -n "${env[$choix]}" ]; then
  echo "Le programme va être lancé avec le choix suivant : ${env[$choix]}"
  selection=${env[$choix]}
  trouver=true
fi

# Fonction de compilation dans le Dockerfile
compilation_Dockerfile(){

    echo "Compilation du projet $name_conteneur dans le Dockerfile "
    echo "Compilation du projet $name_conteneur et création de sont image"
    docker compose build --no-cache

    echo "Création du conteneur $name_conteneur"
    docker compose up -d
    docker compose logs -f
}

compilation_Maven(){

    echo "Compilation du projet $name_conteneur via Maven"

    # Variable d'environnement
    export CONFIG_SERVICE_URI_host="http://192.168.1.68:8089"
    mvn clean package "-Dspring-boot.run.jvmArguments=-Dspring.profiles.active=dev"

    echo "Création de l'images : $name_conteneur"
    docker compose -f docker-compose-dev.yml build --no-cache

    echo "Création du conteneur $name_conteneur"
    docker compose -f docker-compose-dev.yml up -d
    docker compose -f docker-compose-dev.yml logs -f
}

docker info >/dev/null 2>&1
DOCKER_STATUS=$?


if [ $DOCKER_STATUS -eq 0 ]; then
  echo "Docker est en cours d'exécution."

  status=$(docker inspect --format='{{.State.Status}}' $name_conteneur >/dev/null 2>&1)

  # Vérifie l'états du service
  if [[ $status == "running" ]]; then

    timeUTC=$(docker inspect --format='{{.State.StartedAt}}' $name_conteneur)
    conversion=$(date -d $timeUTC)

    # Si il est toujours en cours d'exécution
    echo "************************************"
    echo "Le conteneur $name_conteneur est en cour d'exécution depuis : $conversion"
    echo "Suppression du conteneur $name_conteneur"
    docker compose -f docker-compose.yml logs -f

  elif [[ $status == "exited" ]]; then

    # Si il est toujours en cours d'exécution
    echo "************************************"
    echo "le conteneur $name_conteneur à été stoppé, mais et toujours actif"
    echo "Suppression du conteneur $name_conteneur"
    docker container start $name_conteneur

  else

      if [ $choix -eq 0 ]; then

        compilation_Dockerfile

      elif [ $choix -eq 1  ]; then

        compilation_Maven
      fi


  fi

else
  echo "Docker n'est pas en cours d'exécution."
fi
