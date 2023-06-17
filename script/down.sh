#!/bin/bash

nom_reseau="blog-network"
image="ms-article-service"
mon_conteneur="configuration"

delete_conteneur() {
  # suppression de l'images conteneuriser
  docker rmi $image
  result=$(docker images -f "reference=$image")

  # verification le code de retour de la suppression de l'image
  if [ $? -eq 0 ]; then
    echo "L'images : $image a bien été supprimer "
  else
    echo "L'images : $image n'a pas été supprimer "
  fi
}

delete_reseau() {
  docker network ls --filter "name=$nom_reseau"
  if [[ $? -eq 0 ]]; then
    echo "Le réseau n'est plus actif"
  fi
}

docker compose -f docker-compose-DEV.yml down

# Vérifier si le conteneur n'est plus en cours d'exécution
if [[ -z "$(docker ps -q -f 'status=exited' -f 'name=$mon_conteneur')" ]]; then
  echo "Le conteneur n'est plus en cours d'exécution."

  # Recherche de l'images avant supression
  if [[ $(docker images -q $image) != "" ]]; then

    echo "Suppression du conteneur "
    delete_conteneur
    echo "Etat du réseau $nom_reseau :  "
    delete_reseau

  else
    echo "L'images : $image a déjà était supprimer "
  fi

else
  echo "Le conteneur est toujours en cours d'exécution."
fi

# affichage
echo "Liste des processus en cours d'exécution : "
docker ps -a

echo "Liste des images déployées : "
docker images
