# Migrations SQL — Cycle V2

## Recommandé (une seule commande)

```bash
# 1. Backup
mysqldump -u root -p boulangerie_bd > backup_avant_v2.sql

# 2. Tout appliquer
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_all.sql
```

Le script `migration_v2_all.sql` est **idempotent** (ré-exécutable).

## Fichiers individuels (si besoin)

| Fichier | Contenu |
|---------|----------|
| `migration_v2.sql` | `production_v2`, lignes produit/matière, `lot_v2` |
| `migration_v2_sales.sql` | `livraison_v2` (+ facture), lignes, `vente_libre_v2` |
| `migration_v2_facture_livraison.sql` | ALTER `facture_id` / `facture_numero` |
| `migration_v2_stock_prod.sql` | ALTER `stock_movement.production_v2_id` |
| **`migration_v2_all.sql`** | **Tout le schéma V2** |

Ordre si scripts séparés :

```bash
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_sales.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_facture_livraison.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_stock_prod.sql
```

## Vérification

```sql
SHOW TABLES LIKE '%_v2';
SHOW COLUMNS FROM stock_movement LIKE 'production_v2_id';
SHOW COLUMNS FROM livraison_v2 LIKE 'facture%';
```

Tables attendues :
- `production_v2`, `production_ligne_produit`, `production_ligne_matiere`, `lot_v2`
- `livraison_v2`, `livraison_ligne_v2`, `vente_libre_v2`, `vente_ligne_v2`

## Notes

- Les `CREATE INDEX` peuvent afficher « Duplicate key name » si déjà présents : **ignorer**.
- Les ALTER utilisent `information_schema` : pas d’erreur « Duplicate column ».
- Profil Spring `prod` utilise `ddl-auto=validate` : ces migrations sont **obligatoires** avant démarrage.
