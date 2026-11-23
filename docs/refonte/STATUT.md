# Statut refonte — `refonte/phase-1`

**Dernière mise à jour code :** lots F1–F4 livrés.

## Ouverture de la PR (F5)

Si aucune PR n’apparaît encore, ouvrir en 1 clic :

**https://github.com/hanga77/boulangerie/compare/master...refonte/phase-1?expand=1**

- Base : `master`
- Compare : `refonte/phase-1`
- Recommandé : cocher **Create as draft** jusqu’au pilote terrain

## Checklist avant merge

1. CI verte sur `refonte/phase-1`
2. Backup BDD
3. `docs/refonte/migration_v2_all.sql` + `migration_pilot.sql`
4. Compte Principal (bouton ADMIN dashboard / comptes)
5. Pilote documenté dans `/admin/pilot`
6. Consigne équipe : **Cycle V2 uniquement** pour la journée

## Lots anti-fragilités

| Lot | Statut |
|-----|--------|
| F1 Readiness dashboard | ✅ |
| F2 Bandeaux V1 | ✅ |
| F3 Compte Principal 1 clic | ✅ |
| F4 Guide pilote + pré-vol | ✅ code · journée réelle à faire |
| F5 Merge master | ⏳ PR à ouvrir via le lien ci-dessus |

Voir aussi `MERGE_F5.md`, `GENERATION.md`, `TRAVAUX_FRAGILITES.md`.
