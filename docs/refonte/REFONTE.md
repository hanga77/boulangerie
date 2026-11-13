# Gestiboul — Refonte (Phase 0–1)

Ce dossier contient le code de refonte prêt à intégrer.
Il n'a pas encore remplacé tout le monolithe : il introduit les fondations prioritaires.

## Objectifs livrés

1. Modèle de domaine Production / Stock sans `@ElementCollection` fragile
2. Découplage Stock ↔ Finance via événements métier
3. Use cases applicatifs testables
4. Rôles en enum + durcissement sécurité
5. Licence renforcée (HMAC plus long, salt externalisé)
6. Guide d'intégration

## Structure des packages

```
src/main/java/.../boulangerie/
  domain/           Entités, value objects, événements
  application/      Use cases (orchestration transactionnelle)
  infrastructure/   Sécurité, licence
docs/refonte/       Documentation et migration SQL
```

Voir `INTEGRATION.md` pour brancher les adapters JPA et activer progressivement.
