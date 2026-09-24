# ReboisGabon — Dossier de soutenance

Ce document explique tout le projet : à quoi il sert, qui l'utilise, comment il est construit, pourquoi ces choix, et comment répondre aux questions du jury. À lire en entier avant la soutenance.

---

## 1. Le projet en une minute

**ReboisGabon** est une application de pilotage des programmes de reboisement au Gabon (sujet n°7 du TP INGC2 / MIAGE2, « Reforestation & environnement »).

Elle permet de :
- enregistrer les **sites de reboisement** (localité, province, superficie, statut) ;
- enregistrer les **campagnes de plantation** sur ces sites (essence, date, nombre de plants) ;
- enregistrer les **suivis de croissance** (contrôles de terrain donnant un taux de survie) ;
- **calculer automatiquement le taux de survie moyen par site** : c'est le défi technique du sujet ;
- rechercher et filtrer par **site, essence, période et taux de survie** ;
- visualiser le tout sur une **carte des 9 provinces**, dans un **tableau de bord**, et produire des **rapports PDF et Excel**.

L'idée directrice : **planter ne suffit pas, il faut prouver ce qui survit**.

---

## 2. Qui utilise l'application

L'acheteur visé est l'**administration publique** (direction du reboisement, directions provinciales). Quatre profils l'utilisent au quotidien :

| Rôle | Qui c'est | Ce qu'il fait dans l'application |
|---|---|---|
| **Administrateur** | Responsable informatique ou chef de programme | Crée les comptes (il n'y a pas d'inscription publique), a tous les droits, consulte le journal d'audit |
| **Superviseur** | Coordinateur de programme, chef de brigade | Crée et modifie sites, campagnes, suivis, essences, objectifs ; consulte les finances ; ne supprime rien |
| **Agent de terrain** | Technicien forestier | Saisit les campagnes et les contrôles de croissance au retour du terrain ; consulte les sites ; ne voit pas les finances |
| **Financier** | Responsable administratif et financier | Gère partenaires (bailleurs), financements et budgets ; consulte le terrain sans le modifier |

Les droits exacts sont dans le tableau 2.3 du `GUIDE-DE-TEST.md`.

---

## 3. Architecture imposée : trois niveaux

```
┌──────────────────────┐    HTTP + JSON     ┌──────────────────────────┐    ORM Django    ┌──────────────┐
│  Client JavaFX 21    │ ─────────────────► │  API Django REST         │ ───────────────► │  Base de     │
│  (interface seule)   │ ◄───────────────── │  Framework (logique,     │ ◄─────────────── │  données     │
│  aucun accès BDD     │   jeton JWT        │  sécurité, calculs)      │                  │  PostgreSQL  │
└──────────────────────┘                    └──────────────────────────┘                  └──────────────┘
```

- **Client JavaFX** : uniquement l'interface. Il n'a **aucun pilote JDBC**. Il envoie des requêtes HTTP avec `java.net.http.HttpClient` et lit les réponses JSON avec **Jackson**.
- **API Django REST Framework** : toute la logique métier, l'authentification, les permissions, les filtres, les calculs, les exports.
- **Base de données** : accédée seulement par l'ORM de Django. La cible est **PostgreSQL** (`psycopg2` est installé et la configuration est prête dans `settings.py`) ; en développement, la base est **SQLite**. Le passage de l'une à l'autre se fait en changeant le bloc `DATABASES`, sans toucher au code, puisque tout passe par l'ORM.

**Pourquoi c'est important** : si demain on fait une application mobile pour les agents de terrain, elle réutilisera la même API. La sécurité est appliquée au serveur, pas dans l'interface : même un client modifié ne peut pas contourner les droits.

---

## 4. Technologies utilisées et pourquoi

### 4.1 API (projet `reboisgabon-api`)

| Technologie | Rôle | Pourquoi |
|---|---|---|
| Python 3.11+, **Django 5.2** | Framework web | Imposé ; ORM, migrations, administration intégrée |
| **Django REST Framework 3.17** | API REST | Imposé ; sérialiseurs, ViewSets, pagination, permissions |
| **djangorestframework-simplejwt** | Jetons JWT | Imposé ; authentification sans session serveur |
| **pyotp** (+ django-otp installé) | Codes 2FA TOTP | Imposé (« django-otp ou pyotp ») ; standard RFC 6238, compatible Google Authenticator |
| **django-filter** | Filtres de recherche | Imposé ; filtres déclaratifs combinables |
| **drf-spectacular** | Documentation Swagger | Tester l'API dans le navigateur : `/api/docs/` |
| **django-ratelimit** | Limitation de débit | Bloque le forçage de mot de passe (5 tentatives par minute sur la connexion) |
| **reportlab** / **openpyxl** | Exports PDF / Excel | Rapports pour les partenaires |
| **scikit-learn**, pandas, joblib | Prédiction de survie | Module « intelligence écologique » |
| **qrcode**, Pillow | QR code 2FA, photos | Activation 2FA, photos de suivi |

### 4.2 Client (projet `frontendauthfx`)

| Technologie | Rôle |
|---|---|
| **Java 21, JavaFX 21** (FXML + CSS) | Interface de bureau |
| **java.net.http.HttpClient** | Appels HTTP à l'API (imposé) |
| **Jackson** (+ jsr310) | Lecture et écriture JSON, dates (imposé) |
| **Ikonli** (Material Design 2) | Icônes vectorielles |
| **ZXing** | Génération du QR code 2FA côté client |
| **JavaFX WebView + Leaflet 1.9.4** | Carte interactive (Leaflet est embarqué dans les ressources, pas chargé depuis Internet) |
| **JavaFX Media** | Vidéo de fond de l'écran de connexion |
| Polices **Public Sans**, **Archivo**, **Spectral** | Identité visuelle (licence libre OFL) |
| **Maven** | Compilation et lancement (`mvn javafx:run`) |

---

## 5. Le modèle de données

### 5.1 Les trois entités du sujet

| Entité | Champs principaux | Relation |
|---|---|---|
| **SiteReboisement** | nom, localité, province, superficie (ha), statut (Planifié, En cours, Terminé, Suspendu), latitude, longitude, responsable | Un site a plusieurs campagnes |
| **CampagnePlantation** | site, essence, date de plantation, nombre de plants, responsable | Une campagne a plusieurs suivis |
| **SuiviCroissance** | campagne, date du contrôle, taux de survie (0 à 100 %), plants vivants, observations, prochaine date de contrôle | — |

Contraintes vérifiées : superficie > 0, nombre de plants ≥ 1, taux de survie entre 0 et 100, date de contrôle postérieure à la plantation.

Les identifiants sont des **UUID** (et non des entiers qui se suivent) : on ne peut pas deviner les identifiants des autres enregistrements.

### 5.2 Les entités ajoutées

- **Essence** : le référentiel des espèces (Okoumé, Moabi, Padouk, Kevazingo, Azobé, Movingui, Ozigo, Tali, Acacia, Eucalyptus), avec nom scientifique et croissance rapide ou lente.
- **ObjectifReboisement** : une cible de plants et de survie, de portée nationale, provinciale ou limitée à un site, avec une échéance ; la progression est calculée à partir des campagnes réelles.
- **Partenaire, Financement, BudgetCampagne** : qui finance quoi, montant alloué et coût réel.
- **PhotoSuivi** : les photos prises lors d'un contrôle.
- **Notification** : les alertes (survie critique, contrôle en retard, contrôle à venir, financement reçu).
- **JournalActivite** : l'audit de toutes les actions.
- **User** (rôle), **PasswordResetToken**, **TOTPDevice** : les comptes et la sécurité.

### 5.3 Le défi du sujet : le taux de survie moyen par site

Dans `apps/reforestation/models.py`, propriété `SiteReboisement.taux_survie_moyen` :

```python
SuiviCroissance.objects.filter(campagne__site=self).aggregate(moyenne=Avg('taux_survie'))
```

- Django traduit ceci en **une seule requête SQL** : un `AVG` avec une jointure suivi → campagne → site.
- La moyenne porte sur **tous les contrôles de toutes les campagnes du site**.
- Elle est **recalculée à chaque lecture** : il n'y a pas de valeur stockée qui pourrait devenir fausse. Ajouter un suivi change immédiatement la moyenne affichée (démonstration en direct possible).
- La même logique existe par campagne, par essence et par province pour le tableau de bord et la carte.

### 5.4 Le score écologique (bonus)

Chaque site reçoit une note sur 100, expliquée dans sa fiche sur la carte :

| Critère | Poids |
|---|---|
| Survie des plants | 50 % |
| Régularité des contrôles | 20 % |
| Diversité des essences | 15 % |
| Vitalité (statut du site) | 15 % |

Classes : Excellent (≥ 80), Bon (≥ 60), Moyen (≥ 40), Faible.

---

## 6. L'API

Toutes les routes commencent par `http://localhost:8000/api/`. La documentation interactive est sur `/api/docs/`.

| Domaine | Routes principales |
|---|---|
| Authentification | `auth/login/`, `auth/login/2fa/verify/`, `auth/token/refresh/`, `auth/password/forgot/`, `auth/password/reset/`, `auth/2fa/setup/init/`, `auth/2fa/setup/confirm/`, `auth/2fa/disable/`, `auth/me/`, `auth/mes-permissions/` |
| Compte courant | `me/update/`, `me/change-password/` |
| Utilisateurs (admin) | `users/` (CRUD, recherche, filtres), `users/{id}/reactivate/` |
| Terrain | `sites/`, `campagnes/`, `suivis/`, `photos-suivi/`, `essences/`, `objectifs/` |
| Géographie | `sites-geojson/`, `sites-proximite/?lat=&lon=&rayon_km=`, `sites/{id}/score-ecologique/`, `calendrier-suivis/` |
| Finances | `partenaires/`, `financements/`, `budgets-campagne/` |
| Tableau de bord | `dashboard/overview/`, `sites/`, `essences/`, `provinces/`, `evolution/`, `alertes/`, `responsables/`, `financier/`, `objectifs/`, `scores-ecologiques/`, `carte-provinces/`, `comparaison-periode/` |
| Exports | `exports/rapport-synthese/pdf/`, `exports/rapport-financier/pdf/`, `exports/sites/excel/`, `exports/campagnes/excel/`, `exports/financements/excel/`, `journal/export-csv/` |
| Intelligence | `intelligence/predire-survie/`, `recommander-essence/`, `detection-risque/`, `reentrainer/` |
| Divers | `notifications/`, `journal/`, `health/` |

Conventions :
- **Pagination** : 10 éléments par page (`count`, `next`, `previous`, `results`).
- **Suppression** : il faut ajouter `?confirm=true`, sinon l'API répond 400. C'est l'exigence « suppression avec confirmation » appliquée côté serveur, en plus de la boîte de dialogue du client.
- **Utilisateurs** : la suppression est une **désactivation** (le compte est conservé pour garder l'historique des campagnes qu'il a créées).

### 6.1 Les filtres (django-filter)

Chaque ressource a une classe `FilterSet` dans `apps/reforestation/filters.py`. Les filtres du sujet :

| Filtre demandé | Paramètres |
|---|---|
| par site | `?site=<uuid>` (campagnes et suivis), `?search=` sur le nom |
| par essence plantée | `?essence=<uuid>` ou `?essence_nom=` |
| par période | `?date_debut=` et `?date_fin=` (date de plantation ou date de contrôle) |
| par taux de survie | `?taux_survie_min=` et `?taux_survie_max=` |

Ils se **combinent** : `campagnes/?site=…&essence=…&date_debut=2025-01-01&taux_survie_min=80`. Pour les sites s'ajoutent localité, province, statut et superficie.

---

## 7. La sécurité en détail (30 % de la note)

### 7.1 Connexion

1. Le client envoie `{email, password}` à `auth/login/`.
2. Le mot de passe est comparé à son **empreinte** (hachage **PBKDF2** de Django) : il n'est jamais stocké en clair.
3. Si la 2FA est désactivée : l'API renvoie un jeton **d'accès** (valable 30 min) et un jeton **de rafraîchissement** (valable 1 jour).
4. Le client envoie le jeton d'accès dans chaque requête : `Authorization: Bearer <jeton>`.
5. Quand l'API répond 401 (jeton expiré), le client utilise automatiquement le jeton de rafraîchissement (`auth/token/refresh/`) puis rejoue la requête ; si le rafraîchissement échoue, il revient à l'écran de connexion. Les jetons de rafraîchissement sont renouvelés à chaque usage (rotation).
6. **Limitation** : 5 tentatives de connexion par minute et par adresse IP.

### 7.2 Double authentification (TOTP)

- **Activation** : `2fa/setup/init/` génère un secret aléatoire et une URI `otpauth://` ; le client la transforme en **QR code** (ZXing). L'utilisateur la scanne, puis confirme avec un premier code (`2fa/setup/confirm/`) : la 2FA n'est activée qu'après cette preuve.
- **Connexion** : après le mot de passe, l'API ne donne pas les vrais jetons mais un **jeton temporaire** marqué `2fa_pending`. Le client demande le code à 6 chiffres et l'envoie avec ce jeton à `login/2fa/verify/`. Seulement alors l'API délivre les jetons.
- **Principe TOTP** : le code est calculé à partir du secret partagé et de l'heure (fenêtre de 30 s). Le serveur et le téléphone calculent le même code sans communiquer.

### 7.3 Mot de passe oublié et réinitialisation

1. `password/forgot/` : l'API crée un jeton aléatoire (`secrets.token_urlsafe(48)`), stocké avec une **expiration à 1 heure**, et l'envoie par e-mail.
2. La réponse est **la même que le compte existe ou non** : on ne peut pas découvrir quels e-mails sont inscrits.
3. `password/reset/` : le jeton est vérifié (existant, non utilisé, non expiré), le nouveau mot de passe est enregistré (8 caractères minimum, validateurs Django), et le jeton est **marqué utilisé** : il ne sert qu'une fois.
4. En démonstration, l'e-mail part vers **Mailpit**, un serveur SMTP local qui l'affiche dans une boîte de réception web (`localhost:8025`). Si Mailpit est arrêté, un backend maison (`SmtpOuConsoleBackend`) bascule sur la console au lieu de faire échouer la requête. En production, il suffit de pointer les paramètres `EMAIL_*` du `.env` vers un vrai serveur SMTP.

### 7.4 Pas d'inscription publique

Aucune route d'inscription n'existe. Les comptes sont créés par l'administrateur via `users/`, protégé par la permission `IsAdminRole`.

### 7.5 Les rôles (RBAC)

- La matrice est définie à un seul endroit : `apps/accounts/rbac.py` (`RBAC_MATRIX`).
- La classe `RBACPermission` traduit chaque action (list, retrieve, create, update, destroy) en permission (view, create, edit, delete) et la vérifie pour le rôle de l'utilisateur.
- Pour les agents, `IsOwnerForAgentOrElevated` limite la modification aux enregistrements dont ils sont responsables.
- Le client récupère la matrice (`auth/mes-permissions/`) pour **masquer** les menus et boutons non autorisés. Mais la vraie protection est au serveur : une action interdite renvoie **403**.

### 7.6 Traçabilité

- Un **middleware** mémorise l'utilisateur de la requête ; des **signaux Django** (`post_save`, `post_delete`) écrivent automatiquement dans le journal chaque création, modification et suppression.
- Les connexions, réussies ou échouées, sont aussi tracées, avec l'adresse IP.

---

## 8. Le client JavaFX

### 8.1 Organisation du code

| Paquet | Contenu |
|---|---|
| `api/` | `ApiClient` (HTTP, jeton, rafraîchissement automatique, erreurs) et un client par ressource (`SitesApi`, `CampagnesApi`…) |
| `dto/` | Objets échangés avec l'API, convertis par Jackson (noms en snake_case côté JSON) |
| `session/` | `SessionManager` : jetons, utilisateur connecté, permissions |
| `controllers/` | Un contrôleur par écran FXML |
| `ui/` | Composants partagés : `Composants` (en-têtes, pastilles, barres de survie, étiquettes), `Illustrations` (feuilles botaniques dessinées, courbes de niveau, canopée), `CarteProvinces`, `PanneauVisuel`, `ExportUtil`, `Navigation` |
| `util/` | Navigation entre scènes, fenêtres modales, alertes, caches (sites, essences…) |
| `resources/fxml` | Les écrans |
| `resources/theme/theme.css` | Le thème |
| `resources/carte` | La carte Leaflet (HTML + Leaflet embarqué) |
| `resources/geo` | Les contours des 9 provinces (Natural Earth, domaine public) |

### 8.2 Principes techniques

- **Jamais d'appel réseau sur le fil de l'interface** : chaque appel à l'API se fait dans un fil séparé, puis le résultat est affiché avec `Platform.runLater`. L'interface ne se fige jamais.
- **Navigation** : une coque (`shell.fxml`) avec barre latérale, qui charge chaque écran au centre. Un point unique (`Navigation`) permet à n'importe quel écran d'en ouvrir un autre, par exemple « voir ce site sur la carte ».
- **Carte** : Leaflet tourne dans une `WebView`. Java lui envoie les provinces, les sites et les statistiques ; quand on clique un site, le JavaScript appelle un objet Java (`PontCarte`) qui ouvre la fiche native à droite.
- **Exports** : le client reçoit le fichier binaire (PDF, XLSX, CSV) de l'API et propose de l'enregistrer, puis de l'ouvrir.

### 8.3 Identité visuelle

L'interface a été refondue pour que l'on « sente » le reboisement et qu'elle soit crédible devant une administration :
- **Palette** : vert forêt dominant, or (rappel du drapeau gabonais) pour ce qui est actif ou sélectionné, bleu océan pour les données secondaires, latérite et rouge pour les risques. Fond clair, lisible au bureau et au vidéoprojecteur.
- **Univers « atlas forestier et herbier »** : courbes de niveau et canopée dans la barre latérale, feuilles d'okoumé, de moabi, de padouk et de kevazingo dessinées en vectoriel, fiches de site présentées comme des étiquettes de spécimen, herbier des essences.
- **Couleur de la survie** partout identique : rouge < 60 %, latérite 60-70, or 70-80, vert 80-90, forêt ≥ 90.
- Chaque export est placé **sur la page qu'il concerne** (plus de menu « Exports » séparé).

---

## 9. Les fonctionnalités par écran

| Écran | Contenu |
|---|---|
| **Connexion** | Vidéo de forêt, formulaire, lien mot de passe oublié, 2FA |
| **Tableau de bord** | Arbres plantés, surface, survie, carte des provinces, alertes, plantations mensuelles, comparaison de période, survie par essence, objectifs, classement écologique, financement, équipes ; rapport de synthèse PDF |
| **Carte du territoire** | Provinces colorées par survie, sites (taille = surface, couleur = statut), fiche de site au clic, bilan de province, 3 fonds de carte |
| **Sites** | Registre, filtres, CRUD, export Excel, ouverture sur la carte |
| **Campagnes** | CRUD, les 4 filtres du sujet, export Excel |
| **Suivis de croissance** | CRUD, filtres, photos, calendrier des contrôles en retard et à venir |
| **Essences** | Herbier : une planche par espèce avec ses résultats réels |
| **Objectifs** | Cibles nationale, provinciale ou par site, progression calculée |
| **Finances** | Partenaires, financements, budgets ; rapport financier PDF, financements Excel |
| **Intelligence écologique** | Simulation de survie, essences recommandées, campagnes à risque |
| **Utilisateurs** (admin) | Création, rôle, désactivation, réactivation |
| **Journal d'activité** (admin) | Toutes les actions, filtres, export CSV |
| **Notifications** | Alertes, marquage comme lues |
| **Paramètres** | Profil, mot de passe avec règles, 2FA par QR code, droits du rôle |

### 9.1 Le module d'intelligence écologique

- Modèle **RandomForestRegressor** (scikit-learn, 150 arbres, profondeur 8), entraîné sur l'historique : une ligne par campagne contrôlée.
- Variables d'entrée : essence, province, superficie du site, nombre de plants, mois de plantation, croissance rapide. Variable prédite : taux de survie.
- Un minimum de 20 campagnes contrôlées est exigé pour entraîner. 80 % des données servent à l'apprentissage et 20 % au test.
- Utilisations : estimer la survie **avant** de planter, recommander les essences qui réussissent le mieux dans une province, signaler les campagnes récentes dont la survie prédite passe sous 55 %.
- **À dire honnêtement** : c'est une estimation statistique sur des données de démonstration, qui aide à décider mais ne remplace pas le contrôle de terrain.

---

## 10. Qualité et tests

### 10.1 Trois niveaux de tests

1. **Tests automatisés Django** : `python manage.py test`, **18 tests, tous réussis**. Ils couvrent l'authentification, les filtres combinés, le calcul du taux de survie moyen, la confirmation obligatoire des suppressions et la matrice des rôles.
2. **Scénario de bout en bout sur l'API** : **100 vérifications réussies**. Il crée des comptes pour chaque rôle, fait tout le CRUD, vérifie les filtres, active la 2FA avec un vrai code TOTP, déroule la réinitialisation (jeton invalidé après usage), télécharge chaque export, contrôle la cohérence des totaux et vérifie que chaque site est dessiné **dans sa propre province**.
3. **Scénario dans l'interface réelle** : connexion saisie au clavier, création d'un site par le formulaire, puis d'une campagne, puis d'un suivi à 88 %, vérification que la fiche du site sur la carte affiche 88 %, suppression avec confirmation, mot de passe oublié.

### 10.2 Défauts trouvés et corrigés

Il faut les assumer : ils montrent que les tests servent.

| Défaut | Conséquence | Correction |
|---|---|---|
| Dans les ViewSets, `permission_classes = [IsAuthenticated]` était déclaré après `[RBACPermission]` et l'écrasait | Un agent pouvait créer et supprimer des sites : la matrice des rôles ne s'appliquait pas | Déclaration unique ; 5 tests de rôles ajoutés |
| Sommes calculées à travers la jointure avec les suivis | Totaux de plants par province et par essence multipliés par environ 4 | Sous-requêtes ; les totaux par essence égalent maintenant le total national |
| Le cache du classement des sites stockait une liste mais la vue renvoyait un dictionnaire | Le rapport PDF de synthèse échouait (erreur 500) dans les 3 minutes suivant un premier appel | Cache et réponse homogènes |
| Paramètres de recherche non encodés dans l'URL | Recherche impossible avec un espace ou un accent (« Haut-Ogooué ») | Encodage `URLEncoder` |
| Validation du modèle Objectif non appelée par l'API | Objectif de portée « site » sans site accepté | Validation dans le sérialiseur |
| Coordonnées de démonstration tirées au hasard, mauvaise province pour Lastoursville et Lopé | Sites dessinés hors de leur province, carte incohérente | Coordonnées réelles des localités, contrôle d'appartenance à la province |
| Champ `est_lue` au lieu de `lue` dans le client | Notifications toujours affichées « non lues » | DTO corrigé |
| Aucun lien « Mot de passe oublié » sur l'écran de connexion | Parcours de réinitialisation inaccessible depuis l'interface | Lien ajouté |

---

## 11. Questions probables du jury et réponses

**Pourquoi le client ne se connecte-t-il pas directement à PostgreSQL ?**
C'est interdit par le cahier des charges, et c'est une bonne pratique : la base n'est pas exposée, les règles métier et la sécurité sont centralisées dans l'API, et un autre client (mobile, web) peut réutiliser la même API.

**Comment est calculé le taux de survie moyen par site ?**
Une agrégation `Avg('taux_survie')` sur les suivis dont la campagne appartient au site, traduite en un seul `SELECT AVG(...)` avec jointure. Elle est calculée à la lecture, donc toujours à jour.

**Pourquoi la moyenne des suivis et pas une moyenne pondérée par le nombre de plants ?**
C'est la définition la plus simple et la plus lisible pour un décideur. Une moyenne pondérée serait une évolution possible : il suffirait de pondérer par `nombre_plants` dans l'agrégation.

**Où sont stockés les mots de passe ?**
Seule leur empreinte est stockée (PBKDF2, avec sel). Même l'administrateur ne peut pas les lire.

**Que se passe-t-il si quelqu'un vole le jeton JWT ?**
Il expire au bout de 30 minutes. Le jeton de rafraîchissement dure un jour et change à chaque usage. En production, tout passe en HTTPS ; on activerait aussi la liste noire de simplejwt (application `token_blacklist`) pour invalider immédiatement les anciens jetons à la déconnexion.

**Comment fonctionne la 2FA sans connexion entre le serveur et le téléphone ?**
Les deux partagent un secret (transmis une seule fois par le QR code) et calculent le même code à partir de l'heure, toutes les 30 secondes (standard TOTP, RFC 6238).

**Pourquoi le jeton de réinitialisation expire-t-il et ne sert-il qu'une fois ?**
Pour limiter le risque si l'e-mail est intercepté ou consulté plus tard : passé une heure, ou une fois utilisé, il ne vaut plus rien.

**Comment empêchez-vous un agent de supprimer un site ?**
À deux niveaux : le bouton n'apparaît pas dans le client, et surtout l'API vérifie la matrice des rôles et répond 403, même si la requête est envoyée à la main.

**Pourquoi SQLite en ce moment et pas PostgreSQL ?**
Pour développer sans serveur de base de données. Tout passe par l'ORM : le passage à PostgreSQL consiste à changer le bloc `DATABASES` (configuration déjà écrite, lue depuis `.env`) puis `migrate` et `seed_data`.

**Les données sont-elles réelles ?**
Non : c'est un jeu de démonstration généré par `seed_data` (18 sites, environ 110 campagnes, plus de 500 suivis). Les localités, provinces et essences sont réelles, et les sites sont placés aux coordonnées réelles de leur localité.

**À quoi sert l'intelligence artificielle ici ?**
À estimer la survie d'une future campagne et à recommander des essences par province, à partir de l'historique. C'est une aide à la décision, présentée comme une estimation.

**Comment la carte fonctionne-t-elle dans une application de bureau ?**
Une `WebView` JavaFX affiche la bibliothèque Leaflet, embarquée dans l'application. Les données viennent de l'API au format GeoJSON. Un pont JavaScript → Java ouvre la fiche du site en composants JavaFX natifs.

**Que se passe-t-il sans Internet ?**
L'application fonctionne en réseau local avec l'API. Seuls les fonds de carte viennent d'Internet ; le fond « Épuré » affiche provinces et sites hors ligne.

**Comment avez-vous testé ?**
Tests Django automatisés, scénario de bout en bout sur toute l'API, et scénario joué dans l'interface réelle (voir partie 10), qui ont permis de corriger les défauts listés en 10.2.

---

## 12. Limites et perspectives

| Limite actuelle | Évolution proposée |
|---|---|
| Base SQLite en développement | Bascule sur PostgreSQL et déploiement de l'API en HTTPS |
| Saisie de terrain sur poste fixe | Application mobile hors ligne qui synchronise avec la même API |
| Un site = un point | Contours réels des parcelles (polygones) et calcul de surface |
| E-mails capturés par Mailpit (local) | Configuration SMTP réelle |
| Anciens jetons de rafraîchissement non révoqués (application `token_blacklist` non installée) | Ajouter `rest_framework_simplejwt.token_blacklist` et révoquer le jeton à la déconnexion |
| Modèle entraîné sur données de démonstration | Réentraînement sur données réelles, suivi de la précision |
| Vidéo de connexion issue d'une banque d'images avec filigrane | À remplacer par une vidéo sous licence avant toute diffusion commerciale |

---

## 13. Fichiers utiles

| Fichier | Contenu |
|---|---|
| `docs/GUIDE-DE-TEST.md` | Procédure de test complète, comptes, déroulé de la démonstration |
| `docs/soutenance/ReboisGabon-soutenance.pptx` | Diaporama (14 diapositives, notes de présentation incluses) |
| `docs/soutenance/captures/` | Captures d'écran de l'application |
| `README.md` (client) et `README.md` (API) | Installation et lancement |
| API : `apps/accounts/rbac.py` | Matrice des rôles |
| API : `apps/reforestation/models.py` | Modèle de données et calcul du taux de survie |
| API : `apps/reforestation/filters.py` | Filtres combinables |
| API : `apps/accounts/auth_views.py` | Connexion, 2FA, mot de passe oublié |

---

## 14. Petit glossaire

- **API REST** : service qui expose des données par des adresses web (URL) et des méthodes HTTP (GET lire, POST créer, PATCH modifier, DELETE supprimer).
- **JSON** : format texte d'échange de données entre le client et l'API.
- **JWT** : jeton signé qui prouve l'identité de l'utilisateur à chaque requête.
- **TOTP** : code à usage unique basé sur l'heure (2FA).
- **ORM** : couche qui traduit les objets Python en requêtes SQL.
- **CRUD** : Créer, Lire, Mettre à jour, Supprimer.
- **RBAC** : contrôle d'accès basé sur les rôles.
- **FXML** : fichier XML qui décrit un écran JavaFX.
- **GeoJSON** : format JSON pour les données géographiques (points, polygones).
- **Taux de survie** : pourcentage de plants encore vivants lors d'un contrôle.
