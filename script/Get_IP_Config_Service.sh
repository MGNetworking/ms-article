#!/bin/bash
# NB git bash ne peut lancer ce fichier

destination="docker/.env"

# Récupération de l'adresse IP de la machine
IP=$(hostname -I | awk '{print $1}')
echo "Adresse IP : ${IP}"

# construction de l'URI
URI="http://${IP}:8089"
echo "Adresse URI : ${URI}"

# Vérification si le fichier de destination existe
if [ -f "$destination" ]; then
  # Extraction de la valeur actuelle de IP_DEV
  IP_DEV=$(grep -oP '^IP_DEV=\K.*' "$destination" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
  echo "Valeur actuelle de IP_DEV : ${IP_DEV}"
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
awk -v str="$URI" 'BEGIN {FS=OFS="="} $1=="IP_DEV" && NF==2 {$2=str; copied=1} 1;
END {if (!copied) print "IP_DEV="str}' "$destination" > temp_file && mv temp_file "$destination"

# Vérification de la copie
if grep -q "^IP_DEV=$URI$" "$destination"; then
  echo "La variable a été initialisée avec succès."
  exit 0 # renvoy true
else
  echo "Erreur lors de la copie de la chaîne dans le fichier $destination."
  exit 1 # renvoy false
fi
