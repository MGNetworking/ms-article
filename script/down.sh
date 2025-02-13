#!/bin/bash

# export des variable du fichier .options
#export $(cat .env)
export $(grep -v '^#' .env | xargs)
version_beta=$DOCKER_IMAGE_NAME:$IMAGE_VERSION-beta

handle_error() {
  echo "Une erreur est survenue : $1"
  echo "Fin du script"
  exit 1
}

delete_images() {
  echo "Suppression des images Docker..."

  # Vérifier si la stack est encore présente
  if docker stack ls | grep -q $STACK_NAME ; then
    handle_error "La stack est encore active, impossible de supprimer les images"
  fi

  docker rmi $version_beta >/dev/null 2>&1
  docker rmi "$DOCKER_IMAGE_NAME:$IMAGE_VERSION" >/dev/null 2>&1

  sleep 3  # Laisser un court délai pour éviter des suppressions asynchrones

  echo "Vérification après suppression..."
  if [[ -z $(docker images "$version_beta" -q) && -z $(docker images "$DOCKER_IMAGE_NAME:$IMAGE_VERSION" -q) ]]; then
    echo "✅ Les images ont été supprimées avec succès"
  else
    echo "❌ Échec de la suppression des images !"
    docker images | grep "$DOCKER_IMAGE_NAME"
  fi

}

delete_stack() {

  echo "Suppression de la stack : $STACK_NAME"

  if ! docker stack ls | grep -q "$STACK_NAME"; then
    handle_error "La stack est déjà supprimée !"
  fi

  docker stack rm $STACK_NAME

  # Attente de la suppression complète
  echo "delete image : $DOCKER_IMAGE_NAME:$IMAGE_VERSION et $version_beta"

  sleep 10
  while docker ps -a | grep -q "$STACK_NAME"; do
    sleep 5
  done

  echo "Stack supprimée."
}

delete_all(){
  delete_stack
  delete_images
}

info(){

  echo "Liste des processus en cours d'exécution"
  docker ps -a

  echo "---------------------------"
  echo "Liste des images disponibles :"
  docker images

  echo "---------------------------"
  echo "Liste des réseaux Docker :"
  docker network ls
}


# interface user
options=("Delete stack" "Delete stack and image" "Delete image" "Info docker and continu ...")
echo "Lancement de la compilation (Mode Dev) "
echo "Choisissez votre suppression :"

affichage=""
for i in "${!options[@]}"; do
  affichage+="[$i] ${options[$i]} \n"
done

# Affiche a l'utilisateur tout des options disponibles
echo -e "$affichage"
read choix

trouver=false
selection=""
# recherche du choix sélectionné
if [ -n "${options[$choix]}" ]; then
  echo "-------------[ ${options[$choix]} ]--------------"
  selection=${options[$choix]}
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
