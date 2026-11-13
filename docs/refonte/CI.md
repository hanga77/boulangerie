# CI GitHub Actions — Gestiboul

Fichier : `.github/workflows/ci.yml`

## Quand ça tourne

- Push sur `master`, `main`, `refonte/**`
- Pull request vers `master` / `main`
- Déclenchement manuel (**Actions → CI → Run workflow**)

## Ce que fait le job

1. Checkout
2. JDK **17** (Temurin) + cache Maven
3. `chmod +x ./mvnw`
4. `compile` → `test` → `package`
5. En échec : upload des rapports Surefire
6. En succès : upload du JAR

## Problèmes fréquents et solutions

### 1. Actions grisées / « Workflows aren’t enabled »

Repo → **Settings → Actions → General** → autoriser les workflows (Allow all actions).

### 2. Échec de compilation

- Ouvrir le log rouge **Compile**
- En local :
  ```bash
  git checkout refonte/phase-1
  ./mvnw -B compile
  ```
- Corriger les erreurs Java, puis push

### 3. Échec des tests

- Télécharger l’artifact **surefire-reports** depuis le run GitHub
- Ou en local :
  ```bash
  ./mvnw -B test
  ./mvnw -B test -Dtest=CycleFluxDomaineTest,ProductionV2Test
  ```

### 4. `Permission denied: ./mvnw`

Le workflow fait déjà `chmod +x`. En local :

```bash
chmod +x mvnw
git update-index --chmod=+x mvnw
git commit -m "fix: mvnw executable" && git push
```

### 5. Cache Maven bizarre

Rejouer le workflow avec **Re-run all jobs**, ou temporairement retirer `cache: maven`.

### 6. PR depuis `refonte/phase-1` vers `master`

Le CI se déclenche sur la PR (branches cibles `master`/`main`). Vérifier l’onglet **Checks**.

## Lancer manuellement

GitHub → **Actions** → **CI** → **Run workflow** → branche `refonte/phase-1`.
