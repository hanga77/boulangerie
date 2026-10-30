#!/usr/bin/env bash
# Échoue si un <form method="post"> dans templates/ n'a pas de token CSRF
# dans les 500 caractères suivant la balise d'ouverture.
# Ignore les formulaires entièrement dans un commentaire HTML.
set -euo pipefail

ROOT="${1:-src/main/resources/templates}"
if [[ ! -d "$ROOT" ]]; then
  echo "Répertoire introuvable: $ROOT"
  exit 1
fi

missing=0
while IFS= read -r -d '' file; do
  rel="${file#${ROOT}/}"
  # Fichier entièrement commenté (legacy) : ignorer
  if ! grep -qiE '<form' "$file"; then
    continue
  fi
  # Extraction naïve ligne par ligne des forms post
  mapfile -t lines < "$file"
  in_comment=0
  i=0
  while (( i < ${#lines[@]} )); do
    line="${lines[$i]}"
    # commentaires
    if [[ "$line" == *"<!--"* ]]; then in_comment=1; fi
    if [[ "$line" == *"-->"* ]]; then in_comment=0; fi
    if (( in_comment )); then
      ((i++)) || true
      continue
    fi
    if echo "$line" | grep -qiE '<form[^>]*method[[:space:]]*=[[:space:]]*["'"'']?post'; then
      # fenêtre des 15 lignes suivantes
      window="$line"
      for ((j=i+1; j<i+16 && j<${#lines[@]}; j++)); do
        window+="${lines[$j]}"
      done
      if ! echo "$window" | grep -q '_csrf.token'; then
        echo "CSRF manquant: $rel (ligne $((i+1)))"
        missing=$((missing+1))
      fi
    fi
    ((i++)) || true
  done
done < <(find "$ROOT" -type f -name '*.html' -print0)

if (( missing > 0 )); then
  echo ""
  echo "ÉCHEC: $missing formulaire(s) POST sans token CSRF."
  exit 1
fi

echo "OK: tous les formulaires POST sous $ROOT ont un token CSRF."
