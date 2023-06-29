#!/bin/bash

destination="ms-article/docker/.env"

# Vérifier si xmlstarlet est installé
if ! command -v xmlstarlet &>/dev/null; then
  echo "Installation de xmlstarlet..."
  sudo apt-get update && apt-get install -y xmlstarlet
fi

# Récupérer la version du projet à partir du fichier pom.xml
PROJECT_VERSION=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" -t -v "//x:project/x:version" "ms-article/pom.xml")

# renvoi la valeur
echo "$PROJECT_VERSION"


# Vérification si le fichier de destination existe
if [ -f "$destination" ]; then
  # Extraction de la valeur actuelle de IP_DEV
  version=$(grep -oP '^version=\K.*' "$destination" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
  echo "Valeur actuelle de la version : ${version}"
else
  echo "Le fichier de destination $destination n'existe pas."
  exit 1 # renvoy false
fi

# Vérification des permissions d'écriture sur le fichier
if [ ! -w "$destination" ]; then
  echo "Impossible d'écrire dans le fichier $destination. Vérifiez les permissions."
  exit 1    # renvoy false
fi

# Copie de la chaîne de caractères au bon emplacement
awk -v str="$PROJECT_VERSION" 'BEGIN {FS=OFS="="} $1=="version" && NF==2 {$2=str; copied=1} 1;
END {if (!copied) print "version="str}' "$destination" > temp_file && mv temp_file "$destination"

# Vérification de la copie
if grep -q "^version=$PROJECT_VERSION$" "$destination"; then
  echo "La variable a été initialisée avec succès."
  exit 0 # renvoy true
else
  echo "Erreur lors de la copie de la chaîne dans le fichier $destination."
  exit 1 # renvoy false
fi
