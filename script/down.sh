#!/bin/bash
version="1.0.0"
image="sonatype-nexus.backhole.ovh/ms-article-service"
name_stack="ms-article"

delete_conteneur() {

  echo "************************************"
  echo "delete stack : stack_$name_stack "
  docker stack rm $name_stack

  echo "delete image : $image:$version "
  sleep 10
  docker rmi $image:$version
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
  echo "delete stack : $name_stack "
  docker stack rm $name_stack
}


# interface user
env=("Delete stack $name_stack" "Delete stack $name_stack and delete image $image" )
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
