# Phase 1 — API cycle journalier

Après migration SQL (`docs/refonte/migration_v2.sql`), les endpoints suivants sont disponibles.

## Prérequis

```bash
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2.sql
./mvnw spring-boot:run
```

Les adapters JPA sont actifs par défaut.  
`SecurityConfigV2` / `LicenseServiceV2` restent derrière les profils `refonte-security` et `refonte-license` (pas de collision avec l’existant).

## Enchaînement

### 1. Lancer la production (boulanger)

```http
POST /api/cycle/production/start
Content-Type: application/json

{
  "date": "2026-10-24",
  "farinageMatiereId": 1,
  "farinageQuantite": 2.5
}
```

Réponse : `productionId`, nb lignes matière (recettes + farinage).

### 2. Feuille de sortie (magasinier)

```http
GET /api/cycle/production/{id}/matieres-a-fournir
```

Liste théorique à fournir, stock, reste à sortir, flag farinage.

### 3. Confirmer les sorties (magasinier)

```http
POST /api/cycle/production/{id}/confirmer-sorties
Content-Type: application/json

{}
```

Corps vide = sortir tout le théorique.  
Sinon map `matiereId → quantité` pour ajuster.

### 4. Valider la production (boulanger)

```http
POST /api/cycle/production/{id}/valider
Content-Type: application/json

{
  "quantitesProduits": { "1": 120, "2": 80 },
  "quantitesMatieres": {}
}
```

### 5. Statut du cycle

```http
GET /api/cycle/statut
GET /api/cycle/statut?date=2026-10-24
```

## Notes

- Les mouvements de stock V2 sont tracés avec motif `SORTIE_PROD_V2:{id}` (FK ancienne Production non utilisée).
- Une seule production V2 par date.
- Le farinage saisi au start fait partie du théorique magasinier.
