#!/bin/bash

echo "export des variables "
export $(cat .env)

echo "déploiement / update de la stack : $STACK_NAME"
echo "PROFILES : $PROFILES"

# Demander le suffixe à ajouter (ou utiliser une variable pour le définir)
if [ -z "$1" ]; then
  echo "Aucun suffixe n'a était spécifié. Utilisation du suffixe par défaut 'release' "
  SUFFIX="release"
else
  SUFFIX="$1"
  echo "Utilisation du suffixe spécifié pour la modification du nom de la version: $SUFFIX"
fi

# Modifier la version de l'image avec le suffixe
export IMAGE_VERSION="${IMAGE_VERSION}-${SUFFIX}"
echo "Modification du nom de la version: $IMAGE_VERSION"
echo "Le nom de la stack: $STACK_NAME"
echo "PROFILES: $PROFILES"

# le déploiement sur le nas
if [ "$PROFILES" == "nas" ]; then
  echo "Deploiement avec le PROFILES: NAS !"
  echo "Commande de déploiement sur le serveur Nas"
  echo "/usr/local/bin/docker stack deploy -c ./docker-compose-swarm.yml $STACK_NAME"
else
  echo "Deploiement avec le PROFILES: $PROFILES"
  echo "Commande de déploiement sur le serveur $PROFILES"
  echo "docker stack deploy -c ./docker-compose-swarm.yml $STACK_NAME"
  docker stack deploy -c ./docker-compose-swarm.yml $STACK_NAME
fi

echo "Fin du script de déploiement"
exit 0