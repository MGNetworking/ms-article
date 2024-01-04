#!/bin/bash

nom_reseau="sso_bd"
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

delete_reseau() {

  # Vérifie si le conteneur est actif
  if docker ps -f "network=blog-network" -f "status=running" --format '{{.ID}}' | grep -q .; then
    echo "************************************"
    echo "Un conteneur est toujours actif sur le réseau bridge (blog-network)."
    echo "Le réseau blog-network ne peut être supprimer."
  else
    echo "************************************"
    echo "Les conteneurs ne sont plus actif sur le réseau bridge (blog-network)."
    echo "Le réseau blog-network peut être supprimer "
    docker network rm $nom_reseau
    docker network ls --filter "name=$nom_reseau"

    if [[ $? -eq 0 ]]; then
      echo "************************************"
      echo "Le réseau a été supprimé avec succès."
    else
      echo "************************************"
      echo "Échec de la suppression du réseau $nom_reseau"
    fi
  fi

}

docker compose -f ./docker/docker-compose-DEV.yml down

# Vérifier si le conteneur n'est plus en cours d'exécution
if [[ -z "$(docker ps -q -f 'status=exited' -f 'name='$mon_conteneur)" ]]; then
  echo "************************************"
  echo "Le conteneur n'est plus en cours d'exécution."

  # Obtenir le tag (version) de l'image du conteneur
  tag=$(docker images --filter=reference=$image --format "{{.Tag}}")

  # par nom est son tag get le num de l'images
  if [[ $(docker images -q $image:$tag) != "" ]]; then
    delete_conteneur $image:$tag
    delete_reseau

  else
    echo "************************************"
    echo "L'images : $image a déjà était supprimer "
  fi

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
