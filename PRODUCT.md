# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

Note : client desktop JavaFX 21 (FXML + CSS JavaFX), pas un navigateur. Les règles web s'appliquent par analogie ; la carte est une WebView Leaflet embarquée.

## Users

- Direction et coordinateurs de programme (administration publique) : au bureau, ils suivent le tableau de bord, les objectifs, les rapports et les finances pour piloter le reboisement national.
- Agents de terrain : ils saisissent campagnes de plantation, suivis de croissance et photos au retour du terrain.
- Financiers : ils gèrent partenaires, financements et budgets de campagne.
- Priorité immédiate : la soutenance IAI (INGC2, sujet n°7 ReboisGabon), avec une démo live devant un jury. La vente vient ensuite.

## Product Purpose

ReboisGabon pilote les programmes de reboisement au Gabon : sites, campagnes de plantation, suivis de croissance, avec le calcul automatique du taux de survie moyen par site. Réussir, c'est qu'un décideur voie en un coup d'œil où l'on plante, ce qui survit, ce qui dérive et combien cela coûte.

## Positioning

Un outil de pilotage national du couvert forestier gabonais : les 9 provinces, les essences locales (Okoumé, Moabi, Padouk, Kevazingo…), le score écologique par site et la prédiction de survie par apprentissage automatique.

## Operating Context

- Architecture imposée en 3 niveaux : client JavaFX → API Django REST (JWT + 2FA TOTP) → base de données (SQLite en développement, PostgreSQL visé).
- Aucune inscription publique : les comptes sont créés par l'administrateur.
- Quatre rôles : ADMIN, SUPERVISEUR, AGENT, FINANCIER (matrice de permissions côté API).
- Exports : rapport de synthèse PDF, rapport financier PDF, Excel des sites, des campagnes et des financements.
- Le poste a accès à Internet (fonds de carte en ligne) ; les contours des provinces restent embarqués en repli.

## Capabilities and Constraints

- CRUD : sites, campagnes, suivis (avec photos), essences, objectifs, partenaires, financements, budgets, utilisateurs.
- Filtres attendus : par site, par essence, par période, par taux de survie.
- Tableau de bord analytique, alertes, journal d'audit, notifications, score écologique, IA (prédiction de survie, recommandation d'essence, détection de risque).
- Code sans commentaires (préférence du développeur).

## Brand Commitments

- Nom : ReboisGabon. Langue de l'interface : français.

## Evidence on Hand

- Données de démonstration issues du seed de l'API (18 sites, ~110 campagnes, ~560 suivis, 7 partenaires). Aucun client réel, témoignage ni chiffre commercial : ne pas en inventer.
- Vidéo `src/main/resources/com/reboisgabon/client/media/rebois-login.mp4` (écran de connexion).

## Product Principles

1. Le territoire d'abord : chaque donnée se rattache à un lieu du Gabon (province, localité, site).
2. La survie est la mesure qui compte : planter ne suffit pas, il faut montrer ce qui pousse.
3. Chaque chiffre mène à son détail : on clique, on comprend, on agit ou on exporte depuis la page concernée.
4. La crédibilité institutionnelle avant l'effet : sobre, lisible, digne d'un ministère.
