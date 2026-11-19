# Checklist technique A — avant utilisateurs

Branche : `refonte/phase-1`  
Mise à jour : 2026-10-27

## Statut automatisé (dépôt)

| # | Contrôle | Statut |
|---|----------|--------|
| **A1** | CI verte | ✅ Dernier run CI #31 sur `446f6ea` **success** (Actions branch `refonte/phase-1`) |
| **A3** | Scripts migrations présents | ✅ `docs/refonte/migration_v2_all.sql` (+ FK, sales, stock) |
| A2 / A4 / A5 / A6 | Environnement réel | ⬜ À faire **sur ton serveur / ta BDD** |

Lien Actions : https://github.com/hanga77/boulangerie/actions?query=branch%3Arefonte%2Fphase-1

---

## A1 — CI verte

- [x] Vérifié côté GitHub (success après U5)
- [ ] Optionnel local : `./mvnw -B test`

---

## A2 — Backup BDD

```bash
mysqldump -u USER -p NOM_BDD > backup_avant_v2_$(date +%Y%m%d_%H%M).sql
ls -lh backup_avant_v2_*.sql
```

- [ ] Fichier non vide créé

---

## A3 — Migrations V2

**D’abord sur une copie** de la base :

```bash
# Créer copie (exemple MySQL)
mysql -u USER -p -e "CREATE DATABASE boulangerie_copie;"
mysql -u USER -p boulangerie_copie < backup_avant_v2_....sql

mysql -u USER -p boulangerie_copie < docs/refonte/migration_v2_all.sql
```

Vérifs SQL :

```sql
SHOW TABLES LIKE '%_v2';

SELECT TABLE_NAME, CONSTRAINT_NAME
FROM information_schema.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE()
  AND CONSTRAINT_TYPE = 'FOREIGN KEY'
  AND TABLE_NAME LIKE '%v2%'
ORDER BY TABLE_NAME;
```

Puis seulement sur **prod** si OK.

- [ ] Appliqué sur copie
- [ ] Appliqué sur prod (quand prêt)

---

## A4 — Démarrage sans erreur schéma

```bash
export SPRING_PROFILES_ACTIVE=prod   # ou test
# DB_URL, DB_USERNAME, DB_PASSWORD configurés
./mvnw -B package -DskipTests
java -jar target/gestion-boulangerie-*.jar
```

Logs : **pas** de `Schema-validation: missing table/column`.

- [ ] App démarre

---

## A5 — Secrets

Réf. `docs/refonte/application-prod.properties.example`

```bash
# Générer un salt fort
openssl rand -hex 32
```

Variables attendues :

- `LICENSE_SALT` (long, secret, **pas** la valeur de dev)
- `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`
- `COOKIE_SECURE=true` derrière HTTPS
- Mot de passe **admin** changé

- [ ] Secrets prod renseignés
- [ ] Admin non-défaut

---

## A6 — Compte Principal

Nécessaire pour recettes / dépenses / livraisons V2 automatiques.

```sql
SELECT id, nom, solde FROM compte_bancaire WHERE nom = 'Compte Principal';
-- Si vide : créer via UI finances ou
-- INSERT INTO compte_bancaire (nom, solde) VALUES ('Compte Principal', 0);
```

(Ajuster le nom de table/colonnes si le schéma local diffère.)

- [ ] Compte présent

---

## Résultat

Quand **A1–A6** sont cochés → enchaîner **B. Smoke test cycle V2** (`CHECKLIST_MERGE.md` section B).
