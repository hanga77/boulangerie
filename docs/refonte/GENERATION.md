# Générer / lancer l’application complète

L’application **n’est pas un monolithe à regénérer depuis zéro** :
le code complet vit déjà sur la branche **`refonte/phase-1`**.

Ce document décrit comment **obtenir un JAR exécutable** et le **démarrer** avec le schéma à jour.

## 1. Récupérer le code

```bash
git clone https://github.com/hanga77/boulangerie.git
cd boulangerie
git checkout refonte/phase-1
git pull origin refonte/phase-1
```

## 2. Base de données

```bash
# Création (si besoin)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS boulangerie_bd CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Backup si données existantes
mysqldump -u root -p boulangerie_bd > backup_$(date +%Y%m%d).sql

# Schéma cycle V2 + FK
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_all.sql

# Phase 7 — observations pilote
mysql -u root -p boulangerie_bd < docs/refonte/migration_pilot.sql
```

Ou pack unique :

```bash
mysql -u root -p boulangerie_bd < docs/refonte/migration_complete.sql
```

Vérifier le **Compte Principal** (finances).

## 3. Compiler

```bash
# Java 17+
java -version

./mvnw -B clean package -DskipTests
# ou avec tests :
# ./mvnw -B clean verify
```

Artefact typique :

```text
target/gestion-boulangerie-0.0.1-SNAPSHOT.jar
```
(le nom exact suit `pom.xml`)

## 4. Lancer

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:mysql://localhost:3306/boulangerie_bd?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Douala"
export DB_USERNAME="gestiboul"
export DB_PASSWORD="votre-mot-de-passe"
export LICENSE_SALT="$(openssl rand -hex 32)"

java -jar target/gestion-boulangerie-*.jar
```

Ou profil local (dev) selon `application.properties` du projet.

## 5. Vérifications post-démarrage

| URL | Attendu |
|-----|--------|
| `/login` | Formulaire |
| `/api/public/health` | `{"status":"UP",...}` |
| `/cycle` | Cycle du jour (après auth) |
| `/admin/pilot` | Phase 7 (ADMIN) |

## 6. Périmètre « application complète » livré

| Bloc | Contenu |
|------|--------|
| Métier historique | Commandes, stocks, fournisseurs, guichet, RH, licence |
| Cycle V2 | Production → magasin → livraisons → caisse → clôture |
| Finance unifiée | Trésorerie, dépenses, recettes, marge |
| UI | Design system U0–U5 |
| Pilote | Phase 7 observations |
| Export / multi-comptes / health | Phases 8–10 socle |

## 7. Suite recommandée

1. Checklist technique A  
2. Journée pilote (`/admin/pilot`)  
3. Merge `refonte/phase-1` → `master`  
4. Déploiement serveur (`DEPLOIEMENT.md`)
