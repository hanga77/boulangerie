# Notes de version — Cycle journalier V2

**Branche :** `refonte/phase-1`  
**Périmètre :** phases 1 → 5 de la refonte Gestiboul

## Résumé

Ajout d’un **cycle journalier V2** complet (production → magasin → validation → livraisons / caisse → synthèse / clôture), en parallèle des modules V1 conservés sous **Historique**.

## Nouveautés majeures

### Cycle métier
- Tableau de bord `/cycle` avec checklist d’étapes
- Lancement production depuis commandes + **farinage** dans les matières
- Feuille de sortie magasinier (confirmation quantités)
- Validation quantités produites
- Livraisons V2 liées à `ProductionV2` + **facture** auto + PDF
- Caisse / vente libre V2 (Espèces, Mobile Money)
- Clôture de journée
- Synthèse journalière `/cycle/synthese`
- Historiques : sorties stock, livraisons V2, ventes V2

### Technique
- Entités V2 : production, lots, livraisons, ventes
- CMP sur entrées stock (adapter inventaire)
- Sécurité : rate-limit login, headers CSP, hiérarchie rôles, licence prod obligatoire
- Profil `application-prod.properties`
- CI GitHub Actions (`mvn test`)
- Tests domaine + `@DataJpaTest` ProductionV2

### Migrations SQL
Script unique recommandé :

```bash
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_all.sql
```

Voir [MIGRATIONS.md](./MIGRATIONS.md).

## Modules inchangés (V1)

Commandes, PDV, mon-espace, production/livraisons/ventes **V1**, stocks, fournisseurs, finances, guichet V1, RH, admin, PDF historiques.

## Breaking changes / attention

1. **Profil `prod`** : `LICENSE_SALT` fort obligatoire (sinon refus de démarrage).
2. **`ddl-auto=validate`** en prod : migrations SQL **obligatoires** avant démarrage.
3. Navigation : Production / Livraisons / Ventes V1 sous menu **Historique**.
4. Ne pas utiliser **V1 et V2 en parallèle** le même jour (risque double stock / revenus).

## URLs principales V2

| URL | Rôle |
|-----|------|
| `/cycle` | Pilotage journée |
| `/cycle/lancer` | Démarrer production |
| `/cycle/feuille/{id}` | Magasin |
| `/cycle/valider/{id}` | Validation boulanger |
| `/cycle/livraison/{id}` | Livraisons |
| `/cycle/caisse` | Caisse |
| `/cycle/synthese` | Synthèse |
| `/cycle/sorties` | Sorties stock V2 |
| `/cycle/livraisons-v2` | Historique livraisons |
| `/cycle/ventes-v2` | Historique ventes |

## Documentation

- [CYCLE_JOURNALIER.md](./CYCLE_JOURNALIER.md)
- [DEPLOIEMENT.md](./DEPLOIEMENT.md)
- [MIGRATIONS.md](./MIGRATIONS.md)
- [PHASE1_API.md](./PHASE1_API.md) … [PHASE5.md](./PHASE5.md)

## Compatibilité

- Java 17, Spring Boot 3.4.2
- MySQL 5.7+ / 8+
