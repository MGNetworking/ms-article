#!/bin/bash

BUILD=$1
PROFILES=$2

echo "Exportation des variables"
export $(cat .env)
export PROFILES

######### Vérifier si le fichier ~/.profile existe avant de le charger
if [ -f ~/.profile ]; then
    echo "Chargement et application des variables d’environnement, "
    echo "définies dans le fichier .profile de la session utilisateur"
    source ~/.profile
    echo "✅ Fichier ~/.profile chargé avec succès."
else
    echo "⚠️ Fichier ~/.profile introuvable, chargement ignoré."
fi

######### Gestion des droits sur le système host
echo "Chargement des droites utilisateur du système host"
USER_ID=$(id -u)
GROUP_ID=$(id -g)
export USER_ID
export GROUP_ID

echo "Vérification et/ou création du dossier logs"
LOGS_DIR="$(pwd)/logs"

if [ ! -d "$LOGS_DIR" ]; then
    echo "Le dossier logs n'existe pas. Création en cours..."

    mkdir -p "$LOGS_DIR" || { echo "Erreur : Échec de la création du dossier logs."; exit 1; }
    chmod -R 760 "$LOGS_DIR" || { echo "Erreur : Impossible de modifier les permissions."; exit 1; }
    chown -R ${USER_ID}:${GROUP_ID} "$LOGS_DIR" || { echo "Erreur : Impossible de modifier le propriétaire."; exit 1; }

    echo "✅ Dossier logs créé avec succès !"
else
    echo "✅ Le dossier logs existe déjà."
fi

######## Check la version du build
if [ -z "$BUILD" ]; then
  echo "Aucun suffixe n'a était spécifié. Utilisation du suffixe par défaut 'release' "
  BUILD="release"
fi

echo "Création et export du tag de l'images ${IMAGE_VERSION} sur le version ${BUILD}"
export IMAGE_VERSION="${IMAGE_VERSION}-${BUILD}"
echo "Modification du nom de la version: $IMAGE_VERSION"

echo "Déploiement avec BUILD=$BUILD et PROFILES=$PROFILES de la stack=$STACK_NAME"

######### Le déploiement sur le serveur
echo "🚀 Deploiement avec le PROFILES : " $PROFILES
echo "docker stack deploy -c ./docker-compose-swarm.yml $STACK_NAME"
docker stack deploy -c ./docker-compose-swarm.yml $STACK_NAME

echo "✅ Fin du script de déploiement"
