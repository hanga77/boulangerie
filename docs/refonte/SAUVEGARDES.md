# Sauvegardes automatiques — Gestiboul

Base : **MySQL** (`boulangerie_bd` par défaut).

## Contenu livré

| Fichier | Rôle |
|---------|------|
| `scripts/backup-mysql.sh` | Dump `mysqldump` compressé + rotation |
| `scripts/install-backup-cron.sh` | Installe un cron quotidien |

## 1. Configuration

Créer un fichier d’environnement (recommandé, hors git) :

**Linux prod** — `/etc/gestiboul/backup.env` :

```bash
sudo mkdir -p /etc/gestiboul /var/gestiboul/backups
sudo tee /etc/gestiboul/backup.env << 'EOF'
DB_HOST=localhost
DB_PORT=3306
DB_NAME=boulangerie_bd
DB_USERNAME=gestiboul
DB_PASSWORD=CHANGE_ME
BACKUP_DIR=/var/gestiboul/backups
RETENTION_DAYS=14
# BACKUP_UPLOADS=1
# UPLOAD_DIR=/var/gestiboul/uploads
EOF
sudo chmod 600 /etc/gestiboul/backup.env
```

**Mac / dev** — `~/.gestiboul/backup.env` :

```bash
mkdir -p ~/.gestiboul ~/Downloads/boulangerie/backups
cat > ~/.gestiboul/backup.env << 'EOF'
DB_HOST=localhost
DB_NAME=boulangerie_bd
DB_USERNAME=root
DB_PASSWORD=
BACKUP_DIR=/Users/macbookpro2020/Downloads/boulangerie/backups
RETENTION_DAYS=7
EOF
chmod 600 ~/.gestiboul/backup.env
```

Alternative plus sûre : fichier MySQL defaults (pas de mot de passe en clair dans le process list) :

```ini
# /etc/gestiboul/my.cnf
[client]
user=gestiboul
password=CHANGE_ME
host=localhost
```

Puis `export MYSQL_DEFAULTS_FILE=/etc/gestiboul/my.cnf`.

## 2. Test manuel

```bash
cd /chemin/vers/boulangerie
chmod +x scripts/backup-mysql.sh
./scripts/backup-mysql.sh
ls -lh backups/   # ou /var/gestiboul/backups
```

Fichier attendu : `gestiboul_boulangerie_bd_<host>_YYYYMMDD_HHMMSS.sql.gz`

## 3. Automatisation (cron)

```bash
chmod +x scripts/install-backup-cron.sh
./scripts/install-backup-cron.sh 02:30   # tous les jours à 02:30
crontab -l
```

Ou à la main :

```cron
30 2 * * * cd /opt/gestiboul && /bin/bash scripts/backup-mysql.sh >> /var/gestiboul/backups/backup.log 2>&1
```

Sur macOS, `cron` fonctionne ; on peut aussi utiliser **launchd** si besoin.

## 4. Restauration

```bash
# Arrêter l'application Gestiboul avant si possible
gunzip -c /var/gestiboul/backups/gestiboul_boulangerie_bd_XXX.sql.gz \
  | mysql -u gestiboul -p boulangerie_bd
```

Ou restaurer la base complète créée par `--databases` :

```bash
gunzip -c dump.sql.gz | mysql -u root -p
```

## 5. Bonnes pratiques boulangerie

| Règle | Détail |
|-------|--------|
| Fréquence | **Quotidien** minimum (après clôture du cycle) |
| Rétention | 14 jours local + **copie hebdo hors site** (USB, cloud, autre PC) |
| Avant migration | Toujours un dump manuel (`backup_avant_v2.sql`) |
| Uploads | Activer `BACKUP_UPLOADS=1` si logos / pièces jointes critiques |
| Test restore | Une fois par mois sur une base de test |

## 6. Dépannage

| Symptôme | Action |
|----------|--------|
| `mysqldump: command not found` | Installer client MySQL (`brew install mysql-client` / `apt install mysql-client`) |
| Access denied | Vérifier user/mdp et droits `SELECT, SHOW VIEW, TRIGGER, EVENT, LOCK TABLES` |
| Dump vide / .partial | Espace disque, droits d’écriture sur `BACKUP_DIR` |
| Cron silencieux | Vérifier `backup.log` et que l’utilisateur cron a bien le `PATH` + env |
