#!/usr/bin/env bash
# Installe une tâche cron quotidienne pour scripts/backup-mysql.sh
# Usage: ./scripts/install-backup-cron.sh [HH:MM]
# Exemple: ./scripts/install-backup-cron.sh 02:30
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKUP_SCRIPT="${ROOT}/scripts/backup-mysql.sh"
TIME="${1:-02:30}"

if [[ ! "$TIME" =~ ^([0-1]?[0-9]|2[0-3]):([0-5][0-9])$ ]]; then
  echo "Heure invalide: $TIME (attendu HH:MM)"
  exit 1
fi

HOUR="${TIME%%:*}"
MIN="${TIME##*:}"
# enlever zéro initial pour cron
HOUR=$((10#$HOUR))
MIN=$((10#$MIN))

chmod +x "${BACKUP_SCRIPT}"

# Ligne cron (logs dans BACKUP_DIR)
CRON_LINE="${MIN} ${HOUR} * * * cd ${ROOT} && /bin/bash ${BACKUP_SCRIPT} >> \"${BACKUP_DIR:-${ROOT}/backups}/backup.log\" 2>&1"

# Éviter les doublons
EXISTING="$(crontab -l 2>/dev/null || true)"
if echo "$EXISTING" | grep -Fq "${BACKUP_SCRIPT}"; then
  echo "Une entrée cron existe déjà pour backup-mysql.sh."
  echo "crontab -l  # pour vérifier"
  exit 0
fi

(echo "$EXISTING"; echo "$CRON_LINE") | crontab -
echo "Cron installé: chaque jour à ${TIME}"
echo "  $CRON_LINE"
echo ""
echo "Configure les identifiants via:"
echo "  export DB_USERNAME=... DB_PASSWORD=... BACKUP_DIR=/var/gestiboul/backups"
echo "  ou crée ${HOME}/.gestiboul/backup.env"
