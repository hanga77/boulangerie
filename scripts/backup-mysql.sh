#!/usr/bin/env bash
# =============================================================================
# Gestiboul — sauvegarde MySQL (dump compressé + rotation)
#
# Usage :
#   ./scripts/backup-mysql.sh
#   BACKUP_DIR=/var/gestiboul/backups RETENTION_DAYS=14 ./scripts/backup-mysql.sh
#
# Variables (env ou fichier /etc/gestiboul/backup.env) :
#   DB_HOST       défaut localhost
#   DB_PORT       défaut 3306
#   DB_NAME       défaut boulangerie_bd
#   DB_USERNAME   obligatoire (ou mysql defaults-file)
#   DB_PASSWORD   obligatoire sauf socket root sans mdp
#   BACKUP_DIR    défaut ./backups  (prod: /var/gestiboul/backups)
#   RETENTION_DAYS  défaut 14
#   MYSQL_DEFAULTS_FILE  optionnel, ex. /etc/gestiboul/my.cnf
# =============================================================================
set -euo pipefail

# Charger un fichier d'environnement s'il existe
for envf in /etc/gestiboul/backup.env "${HOME}/.gestiboul/backup.env" ".env.backup"; do
  if [[ -f "$envf" ]]; then
    # shellcheck disable=SC1090
    set -a; source "$envf"; set +a
    break
  fi
done

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-boulangerie_bd}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
STAMP="$(date +%Y%m%d_%H%M%S)"
HOSTNAME_SHORT="$(hostname -s 2>/dev/null || hostname | cut -d. -f1)"
OUT_FILE="${BACKUP_DIR}/gestiboul_${DB_NAME}_${HOSTNAME_SHORT}_${STAMP}.sql.gz"
LOG_TAG="[gestiboul-backup]"

mkdir -p "${BACKUP_DIR}"

log() { echo "${LOG_TAG} $(date '+%Y-%m-%d %H:%M:%S') $*"; }

# Options client MySQL
MYSQL_OPTS=( -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USERNAME}" )
if [[ -n " semiMYSQL_DEFAULTS_FILE:-}" && -f "${MYSQL_DEFAULTS_FILE}" ]]; then
  MYSQL_OPTS=( --defaults-file="${MYSQL_DEFAULTS_FILE}" )
elif [[ -n "${DB_PASSWORD}" ]]; then
  export MYSQL_PWD="${DB_PASSWORD}"
fi

log "Démarrage dump ${DB_NAME} → ${OUT_FILE}"

if ! command -v mysqldump >/dev/null 2>&1; then
  log "ERREUR: mysqldump introuvable (installer mysql-client)"
  exit 1
fi

# Dump cohérent InnoDB
mysqldump "${MYSQL_OPTS[@]}" \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --default-character-set=utf8mb4 \
  --databases "${DB_NAME}" \
  | gzip -c > "${OUT_FILE}.partial"

mv "${OUT_FILE}.partial" "${OUT_FILE}"

SIZE="$(du -h "${OUT_FILE}" | awk '{print $1}')"
log "OK ${OUT_FILE} (${SIZE})"

# Rotation : supprimer les dumps plus vieux que RETENTION_DAYS
if [[ "${RETENTION_DAYS}" =~ ^[0-9]+$ ]] && [[ "${RETENTION_DAYS}" -gt 0 ]]; then
  deleted=0
  while IFS= read -r -d '' f; do
    rm -f "$f"
    deleted=$((deleted + 1))
  done < <(find "${BACKUP_DIR}" -maxdepth 1 -type f -name 'gestiboul_*.sql.gz' -mtime "+${RETENTION_DAYS}" -print0 2>/dev/null || true)
  log "Rotation: ${deleted} fichier(s) > ${RETENTION_DAYS} jours supprimé(s)"
fi

# Option : copier aussi uploads (fichiers joints) si demandé
if [[ "${BACKUP_UPLOADS:-0}" == "1" ]]; then
  UP_SRC="${UPLOAD_DIR:-./uploads}"
  if [[ -d "${UP_SRC}" ]]; then
    UP_OUT="${BACKUP_DIR}/gestiboul_uploads_${HOSTNAME_SHORT}_${STAMP}.tar.gz"
    tar -czf "${UP_OUT}" -C "$(dirname "${UP_SRC}")" "$(basename "${UP_SRC}")"
    log "Uploads: ${UP_OUT}"
  else
    log "WARN: UPLOAD_DIR introuvable (${UP_SRC})"
  fi
fi

log "Terminé."
exit 0
