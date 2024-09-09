#!/bin/bash

# export des variable du fichier .env
export $(cat .env)
version_beta=$DOCKER_IMAGE_NAME:$IMAGE_VERSION-beta

handle_error() {
  echo "Une erreur et survenu lors de l'exécution de la commande : $1"
  echo "Fin du script"
  exit 1
}

delete_images() {
  echo "delete image docker "

  if docker stack ls | grep -q $STACK_NAME ; then
    handle_error "la stack est toujours en cours d'exécution vous ne pouvez supprimer les images"
  fi

  docker rmi $version_beta >/dev/null 2>&1
  docker images -f "reference=$1" >/dev/null 2>&1
  img1=$?

  docker rmi $DOCKER_IMAGE_NAME:$IMAGE_VERSION >/dev/null 2>&1
  docker images -f "reference=$1" >/dev/null 2>&1
  img2=$?

  # verification le code de retour de la suppression de l'image
  if [[ img1 -eq 0 ]] && [[ img2 -eq 0 ]]; then
    echo "Les images docker ont bien été supprimer"
  else
    echo "Les images docker n'ont pas été supprimer"
  fi
}


delete_all() {

  echo "delete stack : $STACK_NAME "
  docker stack ls | grep $STACK_NAME || handle_error "La stack est déjà supprimer !"
  docker stack rm $STACK_NAME

  echo "delete image : $DOCKER_IMAGE_NAME:$IMAGE_VERSION et $version_beta"
  sleep 20

  docker rmi $version_beta
  docker images -f "reference=$1"
  img1=$?
  echo "Sorti $img1"

  docker rmi $DOCKER_IMAGE_NAME:$IMAGE_VERSION
  docker images -f "reference=$1"
  img2=$?
  echo "Sorti $img2"

  # verification le code de retour de la suppression de l'image
  if [[ img1 -eq 0 ]] && [[ img2 -eq 0 ]]; then
    echo "Les images docker sont supprimer"
  else
    echo "Les images docker ne sont pas supprimer"
  fi
}

delete_stack(){
  echo "delete stack : $STACK_NAME "
  docker stack ls | grep $STACK_NAME || handle_error "La stack est déjà supprimer !"
  docker stack rm $STACK_NAME
}

info(){

  echo "Liste des processus en cours d'exécution"
  docker ps -a

  echo "---------------------------"
  echo "Liste des images déployées"
  docker images

  echo "---------------------------"
  echo "List des réseaux"
  docker network ls
}


# interface user
env=("Delete stack" "Delete stack and image" "Delete image" "Info docker and continu ...")
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
  echo "-------------[ ${env[$choix]} ]--------------"
  selection=${env[$choix]}
  trouver=true
fi

if [ $choix -eq 0 ]; then
  delete_stack
elif [ $choix -eq 1  ]; then
  delete_all
elif [ $choix -eq 2  ]; then
  delete_images
elif [ $choix -eq 3  ]; then
  info
fi
