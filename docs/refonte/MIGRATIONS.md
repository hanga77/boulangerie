# Migrations SQL — Cycle V2

## Recommandé (une seule commande)

```bash
mysqldump -u root -p boulangerie_bd > backup_avant_v2.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_all.sql
```

`migration_v2_all.sql` inclut : tables V2, colonnes facture/stock, **et toutes les FK**.

## Fichiers individuels

| Fichier | Contenu |
|---------|----------|
| `migration_v2.sql` | production_v2, lignes, lot_v2 |
| `migration_v2_sales.sql` | livraison_v2, vente_libre_v2 + lignes |
| `migration_v2_facture_livraison.sql` | colonnes facture sur livraison_v2 |
| `migration_v2_stock_prod.sql` | stock_movement.production_v2_id |
| **`migration_v2_fk.sql`** | **FK manquantes** |
| **`migration_v2_all.sql`** | **Tout (schéma + colonnes + FK)** |

Ordre séparé :

```bash
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_sales.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_facture_livraison.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_stock_prod.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_fk.sql
```

## FK ajoutées par `migration_v2_fk.sql`

| Table | Colonne | Référence |
|-------|---------|----------|
| livraison_v2 | user_id | user(id) |
| livraison_v2 | point_de_vente_id | point_de_vente(id) SET NULL |
| livraison_v2 | facture_id | facture(id) SET NULL |
| livraison_ligne_v2 | produit_id | produit(id) |
| vente_libre_v2 | user_id | user(id) |
| vente_libre_v2 | guichet_id | guichet(id) SET NULL |
| vente_ligne_v2 | produit_id | produit(id) |
| lot_v2 | fournisseur_id | fournisseur(id) SET NULL |
| stock_movement | production_v2_id | production_v2(id) SET NULL |

(Déjà présentes dès la création : production → user/produit/matière, liv/vente → production_v2, lignes → parent ON DELETE CASCADE.)

## Vérification

```sql
SHOW TABLES LIKE '%_v2';

SELECT TABLE_NAME, CONSTRAINT_NAME
FROM information_schema.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE()
  AND CONSTRAINT_TYPE = 'FOREIGN KEY'
  AND TABLE_NAME LIKE '%v2%'
ORDER BY TABLE_NAME, CONSTRAINT_NAME;
```

## Notes

- Si une FK échoue pour données orphelines, nettoyer les IDs invalides puis ré-exécuter.
- `CREATE INDEX` en double : message « Duplicate key name » → ignorer.
- Profil `prod` : `ddl-auto=validate` → migrations **obligatoires** avant démarrage.
