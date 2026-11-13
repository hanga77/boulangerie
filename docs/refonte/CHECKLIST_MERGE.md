# Checklist merge `refonte/phase-1` → `master`

## A. Préparation locale

- [ ] `git fetch origin`
- [ ] `git checkout refonte/phase-1 && git pull`
- [ ] `./mvnw -B test` vert
- [ ] Backup BDD : `mysqldump -u root -p boulangerie_bd > backup_pre_merge.sql`
- [ ] Appliquer migrations sur **copie** de BDD :
  ```bash
  mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_all.sql
  ```

## B. Smoke test métier (copie BDD)

### V2 (obligatoire)
- [ ] Login (MANAGER / BOULANGER / MAGASINIER / CAISSIER)
- [ ] Commandes du jour présentes ou créées
- [ ] `/cycle` → lancer production (+ farinage si applicable)
- [ ] Feuille magasin → confirmer sorties
- [ ] Valider quantités
- [ ] Créer une livraison V2 → n° facture + PDF
- [ ] Caisse : une vente Espèces
- [ ] `/cycle/synthese` cohérente
- [ ] Clôturer la journée

### V1 (non-régression minimale)
- [ ] Menu Historique → Production / Livraisons accessibles
- [ ] Entrée stock matières
- [ ] Une page finance ou factures
- [ ] Login toujours OK après plusieurs tentatives (rate-limit)

## C. Merge GitHub

Option PR (recommandé) :

1. Ouvrir une PR : `refonte/phase-1` → `master`
2. Titre suggéré : `Release cycle journalier V2 (phases 1–5)`
3. Description : lien vers `docs/refonte/RELEASE_NOTES.md`
4. CI verte
5. Merge (squash ou merge commit selon habitude)

Option ligne de commande :

```bash
git checkout master
git pull origin master
git merge refonte/phase-1
# résoudre conflits si besoin
./mvnw -B test
git push origin master
```

## D. Tag de release (optionnel)

```bash
git checkout master
git tag -a v2.0.0-cycle -m "Cycle journalier V2 — phases 1 à 5"
git push origin v2.0.0-cycle
```

## E. Production

Suivre [DEPLOIEMENT.md](./DEPLOIEMENT.md) :

- [ ] `migration_v2_all.sql` sur la BDD **prod**
- [ ] `LICENSE_SALT`, `DB_USERNAME`, `DB_PASSWORD`
- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] Redémarrage service + vérifs post-démarrage

## F. Communication équipe

- [ ] Expliquer le menu **Cycle du jour** vs **Historique**
- [ ] Consigne : **un seul flux par jour** (V2 recommandé)
- [ ] Lien notes de version
