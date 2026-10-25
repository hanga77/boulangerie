# Checklist de déploiement — Gestiboul (cycle V2)

Branche recommandée : `refonte/phase-1` (ou merge dans `master` après validation).

## 1. Prérequis serveur

- [ ] Java **17+**
- [ ] MySQL **8+** (ou MariaDB compatible)
- [ ] Au moins **1 Go RAM** libre pour la JVM
- [ ] Accès HTTPS (reverse proxy nginx/Caddy recommandé)
- [ ] Sauvegarde BDD planifiée (dump quotidien)

## 2. Base de données

```bash
# Créer la base si besoin
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS boulangerie_bd CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Migrations refonte (ordre)
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_sales.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_facture_livraison.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_stock_prod.sql
```

Si une colonne existe déjà, ignorer l’erreur MySQL « Duplicate column ».

## 3. Secrets & variables d’environnement

**Ne jamais committer** de mots de passe ni le salt de licence.

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:mysql://localhost:3306/boulangerie_bd?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Douala"
export DB_USERNAME="gestiboul"
export DB_PASSWORD="••••••••"
export LICENSE_SALT="$(openssl rand -hex 32)"   # obligatoire en prod (ProdLicenseGuard)
export UPLOAD_DIR="/var/gestiboul/uploads"
export DOCUMENTS_DIR="/var/gestiboul/documents"
```

Créer les répertoires :

```bash
sudo mkdir -p /var/gestiboul/uploads /var/gestiboul/documents
sudo chown -R $USER:$USER /var/gestiboul
```

## 4. Build

```bash
git checkout refonte/phase-1   # ou master après merge
./mvnw -B clean package -DskipTests
# artefact : target/gestion-boulangerie-0.0.1-SNAPSHOT.jar
```

Ou avec tests :

```bash
./mvnw -B clean test package
```

## 5. Lancement

```bash
java -Xms256m -Xmx768m \
  -Dspring.profiles.active=prod \
  -Dapp.license.salt="$LICENSE_SALT" \
  -jar target/gestion-boulangerie-0.0.1-SNAPSHOT.jar
```

Port par défaut : **9000** (configurable via `SERVER_PORT`).

### systemd (exemple)

Fichier `/etc/systemd/system/gestiboul.service` :

```ini
[Unit]
Description=Gestiboul
After=network.target mysql.service

[Service]
User=gestiboul
WorkingDirectory=/opt/gestiboul
EnvironmentFile=/etc/gestiboul/env
ExecStart=/usr/bin/java -Xms256m -Xmx768m -jar /opt/gestiboul/app.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

`/etc/gestiboul/env` :

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://localhost:3306/boulangerie_bd?...
DB_USERNAME=gestiboul
DB_PASSWORD=...
LICENSE_SALT=...
UPLOAD_DIR=/var/gestiboul/uploads
DOCUMENTS_DIR=/var/gestiboul/documents
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now gestiboul
```

## 6. Reverse proxy (nginx)

```nginx
server {
    listen 443 ssl http2;
    server_name gestiboul.example.com;

    # ssl_certificate ...;

    location / {
        proxy_pass http://127.0.0.1:9000;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Activer les cookies sécurisés uniquement derrière HTTPS (`server.servlet.session.cookie.secure=true` dans le profil prod).

## 7. Vérifications post-démarrage

- [ ] Login OK (`/login`)
- [ ] `/cycle` accessible (rôles atelier)
- [ ] Lancer production V2 + feuille magasin + validation
- [ ] Livraison V2 → n° facture + PDF
- [ ] Caisse `/cycle/caisse`
- [ ] Sorties `/cycle/sorties` après une sortie magasin
- [ ] Sans `LICENSE_SALT` fort → **refus de démarrage** en profil `prod` (attendu)

## 8. Rollback

1. Arrêter le service.
2. Restaurer le JAR précédent.
3. Restaurer un dump MySQL antérieur si schéma incompatible.
4. Les tables `*_v2` peuvent coexister avec V1 : un rollback applicatif ne supprime pas les données V2.

## 9. Flux métier recommandé en prod

```
Commandes → /cycle/lancer (+ farinage)
         → feuille magasin → sorties
         → valider quantités
         → livraisons / caisse
         → clôturer
```

Les modules **Historique** (Production / Livraisons V1) restent pour consultation.
