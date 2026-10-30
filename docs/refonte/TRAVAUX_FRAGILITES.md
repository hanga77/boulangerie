# Travaux anti-fragilités

Branche : `refonte/phase-1`

## Lots code

| Lot | Contenu | Statut |
|-----|---------|--------|
| F1 | Readiness dashboard | ✅ |
| F2 | Bandeaux legacy V1 | ✅ |
| F3 | Compte Principal 1 clic | ✅ |
| F4 | Guide pilote + prérequis | ✅ (code) — **journée réelle restante** |
| F5 | Merge → `master` | PR à ouvrir / merger |

## F4 — déroulé pilote (humain)

1. Backup BDD  
2. `migration_v2_all.sql` + `migration_pilot.sql`  
3. Compte Principal (bouton dashboard ou `/compte-bancaire`)  
4. Ouvrir `/admin/pilot`  
5. Parcourir les 9 étapes du cycle V2 uniquement  
6. Noter OK / Problème  
7. Décider go merge (F5) ou correctifs  

## F5 — merge

```bash
# Après pilote OK
git checkout master && git pull
git merge refonte/phase-1
# ou merger la PR GitHub refonte/phase-1 → master
```

Prérequis merge :

- [ ] CI verte sur `refonte/phase-1`
- [ ] Au moins une demi-journée pilote documentée
- [ ] Compte Principal présent en prod
- [ ] Migrations appliquées

## Consigne équipe

1. Journée = **Cycle du jour** uniquement.  
2. Historique = secours.  
3. Caisse = **Caisse V2**.  
