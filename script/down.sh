#!/bin/bash

image="ms-article-service"
mon_conteneur="article"

delete_conteneur() {
  # suppression de l'images conteneuriser
  docker rmi $1
  docker images -f "reference=$1"

  # verification le code de retour de la suppression de l'image
  if [[ $? -eq 0 ]]; then
    echo "L'images : $1 a bien été supprimer "
  else
    echo "L'images : $1 n'a pas été supprimer "
  fi
}


docker compose down

# Vérifier si le conteneur n'est plus en cours d'exécution
if [[ -z "$(docker ps -q -f 'status=exited' -f 'name='$mon_conteneur)" ]]; then
  echo "************************************"
  echo "Le conteneur n'est plus en cours d'exécution."

  # Obtenir le tag (version) de l'image du conteneur
  tag=$(docker images --filter=reference=$image --format "{{.Tag}}")

  # recherche de l'images
  if [[ $(docker images -q $image:$tag) != "" ]]; then

    delete_conteneur $image:$tag

  else
    echo "************************************"
    echo "L'images : $image a déjà était supprimer "
  fi

  echo "************************************"
  echo "Suppression des images Docker sans étiquette "
  docker image prune -f -a

else
  echo "************************************"
  echo "Le conteneur est toujours en cours d'exécution."
fi

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
