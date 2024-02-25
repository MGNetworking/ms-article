#!/bin/bash

# export des variable du fichier .env
export $(cat .env)

delete_conteneur() {

  echo "************************************"
  echo "delete stack : $STACK_NAME "
  docker stack rm $STACK_NAME

  echo "delete image : $DOCKER_IMAGE_NAME:$IMAGE_VERSION "
  sleep 10
  docker rmi $DOCKER_IMAGE_NAME:$IMAGE_VERSION
  docker images -f "reference=$1"
  echo "Sorti $?"

  # verification le code de retour de la suppression de l'image
  if [[ $? -eq 0 ]]; then
    echo "L'image : $1 a bien été supprimer "
  else
    echo "L'image : $1 n'a pas été supprimer "
  fi
}

delete_stack(){
  echo "************************************"
  echo "delete stack : $STACK_NAME "
  docker stack rm $STACK_NAME
}


# interface user
env=("Delete stack $STACK_NAME" "Delete stack $STACK_NAME and delete image $DOCKER_IMAGE_NAME" )
echo "Lancement de la compilation (Mode Dev) "
echo "Choisissez votre suppression :"

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
  echo "Vous avez sélectionné : ${env[$choix]}"
  selection=${env[$choix]}
  trouver=true
fi

if [ $choix -eq 0 ]; then

  delete_stack

elif [ $choix -eq 1  ]; then

  delete_conteneur
fi


echo "************************************"
echo "Suppression des images Docker sans étiquette "
docker image prune -f -a

# affichage
echo "************************************"
echo "Liste des processus en cours d'exécution : "
docker ps -a

echo "************************************"
echo "Liste des images déployées : "
docker images

echo "************************************"
echo "List des réseaux "
docker network ls
