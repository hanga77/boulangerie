#!/usr/bin/env bash
# Échoue si un <form method="post"> dans templates/ n'a pas de token CSRF
# dans les ~15 lignes suivant la balise d'ouverture.
set -euo pipefail

ROOT="${1:-src/main/resources/templates}"
if [[ ! -d "$ROOT" ]]; then
  echo "Répertoire introuvable: $ROOT"
  exit 1
fi

missing=0
tmp_list=$(mktemp)
find "$ROOT" -type f -name '*.html' > "$tmp_list"

while IFS= read -r file; do
  rel="${file#${ROOT}/}"
  if ! grep -qiE '<form' "$file"; then
    continue
  fi
  mapfile -t lines < "$file"
  in_comment=0
  i=0
  while (( i < ${#lines[@]} )); do
    line="${lines[$i]}"
    if [[ "$line" == *"<!--"* ]]; then in_comment=1; fi
    if [[ "$line" == *"-->"* ]]; then in_comment=0; fi
    if (( in_comment )); then
      i=$((i + 1))
      continue
    fi
    if echo "$line" | grep -qiE '<form[^>]*method[[:space:]]*=[[:space:]]*["'"'']?post'; then
      window="$line"
      for ((j=i+1; j<i+16 && j<${#lines[@]}; j++)); do
        window+="${lines[$j]}"
      done
      if ! echo "$window" | grep -q '_csrf.token'; then
        lineno=$((i + 1))
        echo "CSRF manquant: $rel (ligne ${lineno})"
        missing=$((missing + 1))
      fi
    fi
    i=$((i + 1))
  done
done < "$tmp_list"

rm -f "$tmp_list"

if (( missing > 0 )); then
  echo ""
  echo "ÉCHEC: $missing formulaire(s) POST sans token CSRF."
  exit 1
fi

echo "OK: tous les formulaires POST sous $ROOT ont un token CSRF."
