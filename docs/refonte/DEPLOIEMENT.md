# Checklist de déploiement — Gestiboul (cycle V2)

Branche recommandée : `refonte/phase-1` (ou merge dans `master` après validation).

## 1. Prérequis serveur

- [ ] Java **17+**
- [ ] MySQL **8+** (ou MariaDB / MySQL 5.7 compatible)
- [ ] Au moins **1 Go RAM** libre pour la JVM
- [ ] Accès HTTPS (reverse proxy nginx/Caddy recommandé)
- [ ] Sauvegarde BDD planifiée (dump quotidien)

## 2. Base de données

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS boulangerie_bd CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Backup
mysqldump -u root -p boulangerie_bd > backup_avant_v2.sql

# Migration V2 complète (recommandé)
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_all.sql
```

Détail des scripts : [MIGRATIONS.md](./MIGRATIONS.md).

## 3. Secrets & variables d’environnement

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:mysql://localhost:3306/boulangerie_bd?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Douala"
export DB_USERNAME="gestiboul"
export DB_PASSWORD="••••••••"
export LICENSE_SALT="$(openssl rand -hex 32)"
export UPLOAD_DIR="/var/gestiboul/uploads"
export DOCUMENTS_DIR="/var/gestiboul/documents"
```

```bash
sudo mkdir -p /var/gestiboul/uploads /var/gestiboul/documents
sudo chown -R $USER:$USER /var/gestiboul
```

## 4. Build & lancement

```bash
./mvnw -B clean package -DskipTests
java -Xms256m -Xmx768m -Dspring.profiles.active=prod -jar target/gestion-boulangerie-0.0.1-SNAPSHOT.jar
```

Port : **9000**. Voir aussi `application-prod.properties`.

## 5. Vérifications post-démarrage

- [ ] Login OK
- [ ] `/cycle` + lancer / feuille / valider / livraison / caisse
- [ ] Modules V1 (Historique) encore accessibles
- [ ] Sans `LICENSE_SALT` fort en `prod` → refus de démarrage (attendu)

## 6. Flux métier recommandé

```
Commandes → /cycle/lancer → feuille magasin → valider
         → livraisons / caisse → /cycle/synthese → clôturer
```

Éviter d’utiliser V1 et V2 en parallèle le même jour.
