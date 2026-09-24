# ReboisGabon — client JavaFX

Application de bureau de pilotage des programmes de reboisement au Gabon (sujet n°7, INGC2 / MIAGE2).
Elle consomme exclusivement l'API Django REST `reboisgabon-api` : aucun accès direct à la base de données.

## Prérequis

- JDK 21
- Maven 3.9+
- L'API `reboisgabon-api` démarrée sur `http://localhost:8000` (voir son README)
- Une connexion Internet pour les fonds de carte (les contours des provinces restent affichés hors ligne)

## Lancer l'application

```bash
mvn javafx:run
```

Sous Windows, `run.ps1` détecte le JDK 21 et Maven puis lance la même commande.

L'adresse de l'API se règle dans `src/main/java/com/reboisgabon/client/config/AppConfig.java`.

## Comptes de démonstration

| Rôle | Identifiant | Mot de passe |
|---|---|---|
| Administrateur | `admin@reboisgabon.ga` | `Admin@123` |
| Agents, superviseurs, financiers | générés par `seed_data` (`…@reboisgabon.ga`) | `Agent@123` |

Aucune inscription publique : les comptes sont créés par l'administrateur dans **Administration › Utilisateurs**.

## Parcours

- **Connexion** : e-mail + mot de passe, code TOTP si la double authentification est active, lien « Mot de passe oublié ? » (code à usage unique valable une heure, affiché dans la console de l'API en développement).
- **Tableau de bord** : arbres plantés, surface, survie, carte des provinces colorée selon le taux de survie, alertes terrain, plantations mensuelles, comparaison de période, survie par essence, objectifs, classement écologique, financement, équipes. Export **Rapport de synthèse PDF**.
- **Carte du territoire** : sites placés sur leur localité, taille selon la superficie, couleur selon le statut. Un clic sur un site ouvre sa fiche (étiquette, taux de survie moyen, décomposition du score écologique, campagnes récentes, sites voisins) ; un clic sur une province ouvre son bilan. Fonds Plan, Satellite ou Épuré.
- **Sites, Campagnes, Suivis, Essences, Objectifs** : CRUD complet, recherche, filtres combinables (site, essence, période, taux de survie), suppression avec confirmation. Exports **Excel** des sites et des campagnes sur leur page.
- **Finances** : partenaires, financements, budgets. Exports **Rapport financier PDF** et **Financements Excel**.
- **Intelligence écologique** : simulation de survie avant plantation, essences recommandées par province, campagnes à risque.
- **Administration** : utilisateurs (création, rôle, désactivation, réactivation) et journal d'activité avec export CSV.
- **Paramètres** : profil, mot de passe avec règles de robustesse, activation de la 2FA par QR code, droits du rôle.

## Rôles

Les menus et les actions s'adaptent à la matrice de permissions renvoyée par l'API (`/api/auth/mes-permissions/`) : ADMIN, SUPERVISEUR, AGENT, FINANCIER. L'API applique les mêmes règles côté serveur.

## Organisation du code

| Dossier | Contenu |
|---|---|
| `api/` | Client HTTP (`java.net.http`) et un client par ressource de l'API |
| `dto/` | Objets échangés avec l'API (Jackson, snake_case) |
| `controllers/` | Contrôleurs FXML par module |
| `ui/` | Composants visuels partagés : thème, illustrations botaniques dessinées, carte des provinces, exports |
| `resources/.../fxml` | Écrans |
| `resources/.../theme/theme.css` | Thème (palette forêt, or et océan, polices Public Sans, Archivo et Spectral) |
| `resources/.../carte` | Carte Leaflet embarquée (sans dépendance à un CDN) |
| `resources/.../geo` | Contours des 9 provinces (Natural Earth, domaine public) |

Polices sous licence SIL Open Font License : Public Sans, Archivo, Spectral.
