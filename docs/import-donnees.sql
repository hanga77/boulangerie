-- =============================================================
--  IMPORT DONNÉES — Gestion Boulangerie
--  Compatible : MySQL 8+ / MariaDB 10.5+
--  Importable via : phpMyAdmin, DBeaver, MySQL Workbench
--  ou en ligne de commande : mysql -u root -p gestion_boulangerie < import-donnees.sql
--
--  ⚠  Ce script utilise INSERT IGNORE : il n'écrase pas les
--      données existantes. Pour repartir de zéro, décommentez
--      les blocs TRUNCATE ci-dessous.
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================
--  OPTIONNEL — Vider les tables avant import (décommenter si besoin)
-- =============================================================
-- TRUNCATE TABLE produit_matiere_premiere;
-- TRUNCATE TABLE produit;
-- TRUNCATE TABLE matiere_premiere;
-- TRUNCATE TABLE charge_fixe;
-- TRUNCATE TABLE fournisseur;
-- TRUNCATE TABLE point_de_vente;
-- TRUNCATE TABLE `user`;
-- TRUNCATE TABLE compte_bancaire;

-- =============================================================
--  1. COMPTES BANCAIRES
-- =============================================================
INSERT IGNORE INTO compte_bancaire (id, nom, solde) VALUES
  (1, 'Compte Principal',    500000.00),
  (2, 'Compte Épargne',     1200000.00),
  (3, 'Compte Fournisseurs',      0.00);

ALTER TABLE compte_bancaire AUTO_INCREMENT = 4;

-- =============================================================
--  2. UTILISATEURS
--  Mots de passe (BCrypt, coût 10) :
--    xavier     → admin123
--    manager    → manager123
--    boulanger1 → pain2024
--    boulanger2 → pain2024
-- =============================================================
INSERT IGNORE INTO `user` (id, username, password, role, active, creation_date, email) VALUES
  (1, 'xavier',
   '$2b$10$6smA6vlOUoMeA/d18CGsmewOh0dHjzSPe4hNg8z4qgeXhnQhJwjNG',
   'ADMIN', 1, NOW(), 'xavier@boulangerie.cm'),
  (2, 'manager',
   '$2b$10$6HtIH0/4QS.LanRSRghjZOitjcRG.PVyZyaj2ffqD8YLKQtFfFor6',
   'MANAGER', 1, NOW(), 'manager@boulangerie.cm'),
  (3, 'boulanger1',
   '$2b$10$DNWvmi76g4cWGMz9tWAUh.PtixndwQfAbAfrbKbUee8khUl0kntWK',
   'BOULANGER', 1, NOW(), NULL),
  (4, 'boulanger2',
   '$2b$10$DNWvmi76g4cWGMz9tWAUh.PtixndwQfAbAfrbKbUee8khUl0kntWK',
   'BOULANGER', 1, NOW(), NULL);

ALTER TABLE `user` AUTO_INCREMENT = 5;

-- =============================================================
--  3. MATIÈRES PREMIÈRES
-- =============================================================
INSERT IGNORE INTO matiere_premiere (id, nom, unite_mesure, stock, stock_minimum, prix_unitaire) VALUES
  ( 1, 'Farine de blé',   'kg',   150.0,  20.0,  350.0),
  ( 2, 'Sucre',           'kg',    80.0,  10.0,  600.0),
  ( 3, 'Levure',          'g',   2000.0, 500.0,    4.0),
  ( 4, 'Sel',             'g',   3000.0, 500.0,    0.5),
  ( 5, 'Beurre',          'g',   5000.0, 500.0,    8.0),
  ( 6, 'Lait',            'mL',  8000.0,1000.0,    0.9),
  ( 7, 'Huile végétale',  'mL',  3000.0, 500.0,    1.2),
  ( 8, 'Œufs',            'u',    120.0,  20.0,  150.0),
  ( 9, 'Cacao',           'g',   1500.0, 200.0,    5.0),
  (10, 'Levure chimique', 'g',    800.0, 100.0,    3.5);

ALTER TABLE matiere_premiere AUTO_INCREMENT = 11;

-- =============================================================
--  4. PRODUITS
--  quantite_attendue = production journalière cible
--  quantite_vente_libre_journaliere = quota vente libre (0 = sur commande uniquement)
-- =============================================================
INSERT IGNORE INTO produit (id, nom, prix, quantite_attendue, quantite_vente_libre_journaliere) VALUES
  (1, 'Pain ordinaire',   100.0, 200, 0),
  (2, 'Baguette',         200.0, 150, 0),
  (3, 'Croissant',        350.0,  80, 30),
  (4, 'Pain de mie',      800.0,  60, 20),
  (5, 'Brioche',          500.0,  50, 15),
  (6, 'Gâteau chocolat', 3500.0,  10, 0),
  (7, 'Donut',            250.0, 100, 40);

ALTER TABLE produit AUTO_INCREMENT = 8;

-- =============================================================
--  5. RECETTES (produit → matières premières + quantités)
--
--  Lecture : pour produire 1 unité du produit X,
--  il faut [quantite] unités de la matière première Y.
--  Unités suivent l'unite_mesure de la matière première.
-- =============================================================
INSERT IGNORE INTO produit_matiere_premiere (produit_id, matiere_premiere_id, quantite) VALUES
  -- Pain ordinaire (1) : farine 80g, levure 2g, sel 1.5g, huile 5mL
  (1, 1,  0.08),   -- Farine de blé  (kg)
  (1, 3,  2.00),   -- Levure         (g)
  (1, 4,  1.50),   -- Sel            (g)
  (1, 7,  5.00),   -- Huile          (mL)

  -- Baguette (2) : farine 200g, levure 3g, sel 3g, beurre 10g
  (2, 1,  0.20),
  (2, 3,  3.00),
  (2, 4,  3.00),
  (2, 5, 10.00),

  -- Croissant (3) : farine 50g, beurre 25g, sucre 8g, levure 2g, lait 20mL
  (3, 1,  0.05),
  (3, 5, 25.00),
  (3, 2,  8.00),
  (3, 3,  2.00),
  (3, 6, 20.00),

  -- Pain de mie (4) : farine 250g, beurre 30g, sucre 20g, lait 100mL, levure 5g, sel 5g
  (4, 1,  0.25),
  (4, 5, 30.00),
  (4, 2, 20.00),
  (4, 6,100.00),
  (4, 3,  5.00),
  (4, 4,  5.00),

  -- Brioche (5) : farine 100g, beurre 50g, sucre 30g, œuf 1u, levure 3g
  (5, 1,  0.10),
  (5, 5, 50.00),
  (5, 2, 30.00),
  (5, 8,  1.00),
  (5, 3,  3.00),

  -- Gâteau chocolat (6) : farine 200g, sucre 150g, beurre 100g, œuf 3u, lait 100mL, cacao 50g, levure chimique 5g
  (6, 1,  0.20),
  (6, 2,  0.15),
  (6, 5,100.00),
  (6, 8,  3.00),
  (6, 6,100.00),
  (6, 9, 50.00),
  (6,10,  5.00),

  -- Donut (7) : farine 60g, sucre 15g, œuf 1u, lait 40mL, levure 2g, huile 20mL
  (7, 1,  0.06),
  (7, 2, 15.00),
  (7, 8,  1.00),
  (7, 6, 40.00),
  (7, 3,  2.00),
  (7, 7, 20.00);

-- =============================================================
--  6. CHARGES FIXES
-- =============================================================
INSERT IGNORE INTO charge_fixe (id, type, montant, description, date_echeance, periodicite, paye, compte_bancaire_id) VALUES
  (1,  'LOYER',       150000, 'Loyer local boulangerie',           DATE_ADD(LAST_DAY(NOW()), INTERVAL 1 DAY), 'MENSUEL',      0, 1),
  (2,  'ELECTRICITE',  45000, 'Facture électricité ENEO',          DATE_ADD(LAST_DAY(NOW()), INTERVAL 1 DAY), 'MENSUEL',      0, 1),
  (3,  'EAU',          15000, 'Facture eau CDE',                   DATE_ADD(LAST_DAY(NOW()), INTERVAL 1 DAY), 'MENSUEL',      0, 1),
  (4,  'SALAIRE',      80000, 'Salaire boulanger 1',               DATE_ADD(LAST_DAY(NOW()), INTERVAL 1 DAY), 'MENSUEL',      0, 1),
  (5,  'SALAIRE',      80000, 'Salaire boulanger 2',               DATE_ADD(LAST_DAY(NOW()), INTERVAL 1 DAY), 'MENSUEL',      0, 1),
  (6,  'SALAIRE',     120000, 'Salaire manager',                   DATE_ADD(LAST_DAY(NOW()), INTERVAL 1 DAY), 'MENSUEL',      0, 1),
  (7,  'ASSURANCE',    25000, 'Assurance locaux et équipements',   DATE_ADD(CURDATE(), INTERVAL 3 MONTH),     'TRIMESTRIEL',  0, 1),
  (8,  'MAINTENANCE',  30000, 'Entretien four et pétrin',          DATE_ADD(CURDATE(), INTERVAL 6 MONTH),     'SEMESTRIEL',   0, 1),
  (9,  'ELECTRICITE',  45000, 'Facture électricité — mois précédent', DATE_SUB(CURDATE(), INTERVAL 5 DAY),   'MENSUEL',      1, 1),
  (10, 'EAU',          15000, 'Facture eau — mois précédent',      DATE_SUB(CURDATE(), INTERVAL 5 DAY),      'MENSUEL',      1, 1);

ALTER TABLE charge_fixe AUTO_INCREMENT = 11;

-- =============================================================
--  7. FOURNISSEURS
-- =============================================================
INSERT IGNORE INTO fournisseur (id, nom, adresse, telephone, email, reference_contact, categorie_produits) VALUES
  (1, 'SOSUCAM',             'Njombé, Littoral',       '+237 233 000 001', 'commercial@sosucam.cm',  'M. Mbarga Paul', 'Sucre, céréales'),
  (2, 'Grands Moulins',      'Douala, Bonapriso',      '+237 233 421 100', 'ventes@gmcam.cm',        'Mme Ngo Epée',   'Farine, blé, semoule'),
  (3, 'Laiterie du Berger',  'Yaoundé, Mfandena',      '+237 222 310 045', 'contact@laiterie.cm',    'M. Djoumessi',   'Lait, beurre, produits laitiers'),
  (4, 'PromoPack',           'Douala, Akwa',           '+237 699 123 456', 'info@promopack.cm',      'Mme Bello Awa',  'Emballages, sachets alimentaires'),
  (5, 'AgroDistrib',         'Yaoundé, Melen',         '+237 677 654 321', 'achats@agrodistrib.cm',  'M. Nkolo',       'Œufs, farine, levure, épices');

ALTER TABLE fournisseur AUTO_INCREMENT = 6;

-- =============================================================
--  8. POINTS DE VENTE
-- =============================================================
INSERT IGNORE INTO point_de_vente (id, nom, adresse, type, actif) VALUES
  (1, 'Boutique Centrale',   'Rue de la Paix, Yaoundé Centre',  'BOUTIQUE',        1),
  (2, 'Marché Mokolo',       'Marché Mokolo, Yaoundé',           'MARCHE',          1),
  (3, 'Marché Mfoundi',      'Marché central Mfoundi, Yaoundé',  'MARCHE',          1),
  (4, 'Dépôt Essos',         'Quartier Essos, Yaoundé',          'DEPOT',           1),
  (5, 'Guichet Boulangerie', 'Boulangerie principale, Yaoundé',  'GUICHET_CENTRAL', 1);

ALTER TABLE point_de_vente AUTO_INCREMENT = 6;

-- =============================================================
SET FOREIGN_KEY_CHECKS = 1;
-- =============================================================
--  FIN D'IMPORT
--  Connexion : xavier / admin123
-- =============================================================
