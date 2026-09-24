# ReboisGabon — Guide de test et de démonstration

Ce guide décrit, pas à pas, comment lancer l'application et vérifier chacune de ses fonctionnalités, avec les comptes à utiliser. Il se termine par le déroulé conseillé pour la démonstration de soutenance.

---

## 1. Démarrer l'application

### 1.1 L'API (à lancer en premier)

Dans un terminal, depuis le dossier `reboisgabon-api` :

```bash
venv\Scripts\activate
python manage.py runserver
```

L'API répond sur `http://localhost:8000`. Vérification rapide : ouvrir `http://localhost:8000/api/health/` dans un navigateur, ou la documentation Swagger sur `http://localhost:8000/api/docs/`.

### 1.1 bis La boîte mail de démonstration (Mailpit)

Les e-mails de l'API (code de réinitialisation, alertes critiques) sont envoyés à **Mailpit**, un faux serveur SMTP local qui les affiche dans une vraie boîte de réception web. Aucun mail ne sort de la machine.

Installation (une seule fois) :

```bash
winget install axllent.mailpit
```

Puis, dans un terminal séparé, avant la démonstration :

```bash
mailpit
```

Boîte de réception : `http://localhost:8025`. L'API envoie sur `localhost:1025` (réglages `EMAIL_*` du fichier `.env`).

Si Mailpit n'est pas lancé, rien ne casse : l'e-mail s'affiche alors dans le terminal de l'API, comme avant.

### 1.2 Le client JavaFX

Dans un second terminal, depuis le dossier `frontendauthfx` :

```bash
mvn javafx:run
```

ou, sous Windows, `.\run.ps1` (détecte automatiquement le JDK 21 et Maven).

### 1.3 Repartir d'une base propre (optionnel)

```bash
python manage.py seed_data --flush
python manage.py entrainer_ia
```

`seed_data` recrée les comptes, les 10 essences, les 18 sites, les campagnes, les suivis, les partenaires, les objectifs et les notifications, puis harmonise les données (e-mails, statuts, montants). **Attention** : il régénère aussi le secret 2FA du compte de démonstration (voir 2.2).

Sur une base déjà existante, deux commandes remettent les données au propre sans tout effacer :

```bash
python manage.py recaler_coordonnees_sites
python manage.py harmoniser_donnees_demo
```

---

## 2. Les comptes de démonstration

### 2.1 Comptes par rôle

| Rôle | E-mail | Mot de passe | Ce que ce compte peut faire |
|---|---|---|---|
| **Administrateur** | `admin@reboisgabon.ga` | `Admin@123` | Tout : utilisateurs, journal d'activité, suppression de n'importe quelle donnée, finances, IA |
| **Superviseur** | `sylvie.nguema@reboisgabon.ga` | `Agent@123` | Crée et modifie sites, campagnes, suivis, essences, objectifs. Voit les finances en lecture. Ne supprime pas. |
| **Superviseur** | `nadege.nzue@reboisgabon.ga` | `Agent@123` | Idem |
| **Agent de terrain** | `pierre.ndong@reboisgabon.ga` | `Agent@123` | Consulte le terrain, crée et modifie campagnes et suivis. Ne voit ni les finances ni l'administration. |
| **Agent de terrain** | `paul.mba@reboisgabon.ga`, `alice.mabika@reboisgabon.ga`, `judith.moussavou@reboisgabon.ga`… | `Agent@123` | Idem |
| **Financier** | `sylvie.assengone@reboisgabon.ga` | `Agent@123` | Gère partenaires, financements, budgets. Consulte le terrain sans le modifier. |
| **Superviseur avec 2FA** | `demo2fa@reboisgabon.ga` | `Demo2fa@123` | Compte dédié à la démonstration de la double authentification |

La liste complète des comptes est visible dans **Administration › Utilisateurs** (connecté en administrateur).

### 2.2 Préparer le compte 2FA (avant la soutenance)

1. Installer **Google Authenticator** ou **Microsoft Authenticator** sur un téléphone.
2. Dans l'application mobile : « Ajouter un compte » › « Saisir une clé de configuration ».
3. Nom : `ReboisGabon demo2fa`, clé : le secret affiché par `seed_data` dans le terminal, ligne `>>> Secret TOTP à ajouter…`.
   Sur la base actuelle, ce secret vaut `3MWDN5VXKRIE6ZAFUYAVOURUL7CJ3URE`.
4. Type : « basé sur l'heure ». Le téléphone affiche alors un code à 6 chiffres qui change toutes les 30 secondes.

Pour relire le secret à tout moment :

```bash
python manage.py shell -c "from apps.accounts.models import TOTPDevice; print(TOTPDevice.objects.get(user__email='demo2fa@reboisgabon.ga').secret)"
```

### 2.3 La matrice des rôles

| Domaine | Administrateur | Superviseur | Agent | Financier |
|---|---|---|---|---|
| Sites | tout | consulter, créer, modifier | consulter | consulter |
| Campagnes | tout | consulter, créer, modifier | consulter, créer, modifier | consulter |
| Suivis de croissance | tout | consulter, créer, modifier | consulter, créer, modifier | consulter |
| Essences | tout | consulter, créer, modifier | consulter | consulter |
| Objectifs | tout | consulter, créer, modifier | consulter | consulter |
| Finances | tout | consulter | — | tout |
| Intelligence écologique | consulter, réentraîner | consulter, réentraîner | consulter | — |
| Utilisateurs, journal | tout | — | — | — |

Cette matrice est appliquée **par l'API** (réponse 403 si l'action est interdite) et reprise par le client (menus et boutons masqués). On la retrouve dans l'application : **Paramètres › Session et permissions**.

---

## 3. Scénarios de test, fonctionnalité par fonctionnalité

Chaque scénario indique le compte à utiliser, les actions et le résultat attendu.

### 3.1 Authentification (30 % de la note)

**A. Connexion simple — admin**
1. Écran de connexion : saisir `admin@reboisgabon.ga` / `Admin@123`, cliquer **Se connecter** (ou touche Entrée).
2. Attendu : le tableau de bord s'ouvre, le nom et le rôle apparaissent en bas de la barre latérale.

**B. Mauvais mot de passe**
1. Saisir `admin@reboisgabon.ga` / `mauvais`.
2. Attendu : message rouge « Identifiants incorrects… ». Au-delà de 5 tentatives par minute depuis le même poste, l'API bloque temporairement.

**C. Double authentification (2FA)**
1. Se connecter avec `demo2fa@reboisgabon.ga` / `Demo2fa@123`.
2. Attendu : écran **Vérification en deux étapes**.
3. Saisir un code faux (`000000`) : message d'erreur.
4. Saisir le code affiché par l'application Authenticator : connexion réussie.

**D. Activer la 2FA sur son propre compte**
1. Connecté (par exemple en superviseur `sylvie.nguema@…`), ouvrir **Paramètres › Double authentification**.
2. Cliquer **Activer la double authentification** : un QR code et une clé manuelle s'affichent.
3. Scanner le QR code avec Authenticator, saisir le code, cliquer **Confirmer l'activation**.
4. Attendu : pastille « Activée ». Se déconnecter puis se reconnecter : le code est maintenant demandé.
5. Pour revenir à l'état initial : **Paramètres › Double authentification › Désactiver**.

**E. Mot de passe oublié et réinitialisation**
1. Écran de connexion › lien **Mot de passe oublié ?**
2. Saisir `pierre.ndong@reboisgabon.ga`, cliquer **Recevoir le code**.
3. Attendu : message « Si ce compte existe, un code vient de vous être envoyé… » (même message si le compte n'existe pas, pour ne rien révéler).
4. Ouvrir la boîte **Mailpit** (`http://localhost:8025`) : l'e-mail « ReboisGabon — Réinitialisation de mot de passe » est arrivé. Copier le code (longue chaîne sous « Voici votre code de réinitialisation »).
5. Cliquer **J'ai déjà reçu un code**, coller le code, saisir un nouveau mot de passe (8 caractères minimum), valider.
6. Attendu : message « Mot de passe mis à jour », retour à la connexion ; le nouveau mot de passe fonctionne.
7. Réutiliser le même code : refusé (« invalide, expiré ou déjà utilisé »). Le code expire aussi au bout d'une heure.
8. Pensez à remettre `Agent@123` via **Paramètres › Mot de passe** si vous réutilisez ce compte.

**F. Pas d'inscription publique**
Aucun bouton « Créer un compte » sur l'écran de connexion ; l'API n'expose pas de route d'inscription. Les comptes se créent uniquement en 3.2.

**G. Déconnexion**
Icône de sortie à droite du nom (barre latérale) ou **Paramètres › Session et permissions › Se déconnecter** : retour à l'écran de connexion.

### 3.2 Module Utilisateurs (15 %) — compte administrateur

1. **Administration › Utilisateurs**.
2. **Nouvel utilisateur** : prénom, nom, e-mail, rôle, mot de passe (8 caractères minimum). Enregistrer.
3. Attendu : le compte apparaît dans la liste. Tester la **recherche** (nom ou e-mail) et les filtres **Rôle** et **Statut**.
4. **Modifier** (icône crayon) : changer le rôle, enregistrer.
5. **Désactiver** : confirmation demandée ; le compte passe « Désactivé » et ne peut plus se connecter.
6. **Réactiver** : le compte peut de nouveau se connecter.
7. Se connecter avec un compte agent : le menu **Administration** n'existe pas.

### 3.3 Sites de reboisement — CRUD

Compte : administrateur (ou superviseur pour tout sauf la suppression).

1. **Terrain › Sites de reboisement** : liste paginée avec province, superficie, nombre de campagnes, statut et survie moyenne.
2. **Nouveau site** : nom, localité, province (liste des 9 provinces), superficie, statut, latitude/longitude, responsable. Laisser le nom vide puis enregistrer : message de validation.
3. Remplir correctement, par exemple « Site de test », Ndjolé, Moyen-Ogooué, 42,5 ha, En cours, -0,18 / 10,77.
4. **Modifier** : le formulaire s'ouvre pré-rempli ; changer la superficie et enregistrer.
5. **Voir sur la carte** (icône carte, ou double-clic sur la ligne) : la carte s'ouvre centrée sur le site avec sa fiche.
6. **Supprimer** (administrateur uniquement) : une fenêtre de confirmation rappelle que les campagnes et suivis liés seront supprimés.
7. **Exporter Excel** : enregistre le fichier `.xlsx` des sites et propose de l'ouvrir.

### 3.4 Campagnes de plantation — CRUD et les 4 filtres du sujet

1. **Terrain › Campagnes de plantation**.
2. **Nouvelle campagne** : site, essence, date de plantation, nombre de plants, responsable.
3. Les **4 filtres demandés par le sujet**, combinables :
   - **par site** (liste « Tous les sites ») ;
   - **par essence plantée** (liste « Toutes les essences ») ;
   - **par période** (« Planté du » … « au ») ;
   - **par taux de survie** (« Survie ≥ 50 / 70 / 80 / 90 % »).
4. Exemple : site « Réserve de Lastoursville » + survie ≥ 80 % : seules les campagnes correspondantes restent. **Effacer** remet tout à zéro.
5. **Exporter Excel** : fichier des campagnes.

### 3.5 Suivis de croissance et défi du sujet (taux de survie moyen par site)

1. **Terrain › Suivis de croissance › Contrôles effectués**.
2. **Nouveau suivi** : campagne, date du contrôle, taux de survie (%), plants vivants, observations.
3. Contrôles automatiques : une date antérieure à la plantation est refusée ; un taux supérieur à 100 % est refusé.
4. **Vérifier le calcul automatique** :
   - noter la survie moyenne du site dans **Sites** (ou sur sa fiche de la carte) ;
   - ajouter un suivi sur une campagne de ce site ;
   - revenir sur le site : la moyenne a changé immédiatement. C'est la moyenne de tous les contrôles de toutes les campagnes du site.
5. Filtres : période du contrôle, taux minimum et maximum.
6. Onglet **Calendrier des prochains contrôles** : contrôles en retard et à venir (7 à 90 jours).
7. Icône **Photos** sur un suivi : photos de terrain associées au contrôle.

### 3.6 Carte du territoire

1. **Pilotage › Carte du territoire**.
2. Attendu : les 9 provinces colorées selon la survie moyenne ; les sites sous forme de points (taille = superficie, couleur = statut, pointillé = planifié).
3. Survoler un point : nom, localité, superficie, survie.
4. **Cliquer un site** : la fiche s'ouvre à droite, avec l'étiquette du site, la jauge de survie (repères 50/70/90), le score écologique détaillé, les campagnes récentes, les sites à moins de 80 km et les boutons **Voir les campagnes** / **Modifier**.
5. **Cliquer une province** : son bilan (sites, hectares, plants, survie) et la liste de ses sites.
6. **Recentrer** : retour à la vue nationale.
7. Fonds de carte : **Plan** (topographique, par défaut), **Satellite**, **Épuré** (sans fond, fonctionne hors ligne).

### 3.7 Tableau de bord

1. **Pilotage › Tableau de bord** (écran d'accueil).
2. Vérifier les blocs : bandeau (arbres plantés, surface, survie, sites, contrôles), carte des provinces (clic = ouverture de la carte sur la province), alertes terrain, plantations mensuelles, rythme mois/année (bascule **Mois / Année**), survie par essence, objectifs, classement écologique (clic sur un site = sa fiche), financement (administrateur et financier), équipes de terrain.
3. **Rapport de synthèse** (en haut à droite du bandeau) : génère et enregistre le PDF.

### 3.8 Essences (herbier)

1. **Terrain › Essences** : une planche par espèce avec son nom scientifique, sa croissance, ses campagnes, ses plants et sa survie réelle.
2. Recherche par nom ou nom scientifique.
3. **Nouvelle essence**, modification, suppression. Une essence utilisée par des campagnes ne peut pas être supprimée : l'API refuse et le message l'explique.

### 3.9 Objectifs de reboisement

1. **Programme › Objectifs**.
2. **Nouvel objectif** : portée National / Province / Site, cible de plants, survie minimale visée, dates.
3. Contrôles : une portée « Site » sans site est refusée ; une échéance avant la date de début est refusée.
4. La progression (plants réalisés / cible) et le statut (En cours, Atteint, Non atteint) sont calculés automatiquement à partir des campagnes.

### 3.10 Finances — compte administrateur ou financier

1. **Programme › Finances** : onglets **Partenaires**, **Financements**, **Budgets de campagne**.
2. Créer un partenaire, puis un financement affecté **soit** à une campagne **soit** à un site (les deux à la fois est refusé).
3. Créer ou mettre à jour le budget d'une campagne (montant alloué / montant réel).
4. Exports en haut de page : **Rapport financier** (PDF) et **Financements Excel**.
5. Se connecter en agent : le menu **Finances** n'apparaît pas.

### 3.11 Intelligence écologique

1. **Programme › Intelligence écologique**.
2. **Simuler une campagne** : essence, province, superficie, nombre de plants, mois. Cliquer **Estimer la survie** : taux prédit, verdict (satisfaisant ou à risque) et conseil.
3. **Essences recommandées** : choisir une province, les essences sont classées par survie observée.
4. **Campagnes à risque** : liste des campagnes récentes dont la survie prédite passe sous le seuil.
5. **Réentraîner le modèle** (administrateur, superviseur) : recalcule le modèle avec les derniers suivis.

### 3.12 Notifications

1. Cloche en haut à droite : le badge indique le nombre de notifications non lues.
2. La page liste les alertes (survie critique, suivi en retard, contrôle à venir, financement).
3. **Marquer comme lue** sur une notification, ou **Tout marquer comme lu** : le badge se met à jour.

### 3.13 Journal d'activité — administrateur

1. **Administration › Journal d'activité** : chaque création, modification, suppression, connexion (réussie ou échouée) avec l'auteur, la date et l'adresse IP.
2. Filtres : texte, action, modèle, période.
3. **Exporter CSV**.

### 3.14 Paramètres du compte

1. **Paramètres › Profil** : modifier prénom et nom ; e-mail et rôle en lecture seule.
2. **Mot de passe** : ancien, nouveau, confirmation, avec les règles cochées en direct (8 caractères, majuscule et minuscule, chiffre, confirmation identique).
3. **Double authentification** : voir 3.1 D.
4. **Session et permissions** : compte connecté, serveur utilisé, et tableau des droits du rôle.

### 3.15 Contrôle des droits par rôle (à faire en fin de test)

| Se connecter en… | Vérifier |
|---|---|
| Agent (`pierre.ndong@…`) | Pas de menu Finances ni Administration ; pas de bouton « Nouveau site » ; pas d'icône de suppression sur les sites |
| Financier (`sylvie.assengone@…`) | Menu Finances présent ; pas de bouton « Nouvelle essence » ; pas d'Administration |
| Superviseur (`sylvie.nguema@…`) | Peut créer un site, mais aucune icône de suppression |
| Administrateur | Tout est visible |

---

## 4. Tests automatisés

Depuis `reboisgabon-api` :

```bash
python manage.py test
```

Attendu : `Ran 18 tests … OK`. Les tests couvrent l'authentification, les filtres combinables, le calcul du taux de survie moyen par site, la confirmation obligatoire des suppressions et la matrice des rôles.

---

## 5. Démonstration de soutenance (10 à 15 minutes)

Préparation : API lancée, client lancé sur l'écran de connexion, téléphone avec Authenticator configuré (2.2), base harmonisée (1.3).

| Temps | Action | Ce qu'on montre |
|---|---|---|
| 0:00 | Diapositives 1 à 6 | Contexte, cahier des charges, architecture, modèle, sécurité |
| ~6:00 | Connexion `demo2fa@…` puis code du téléphone | 2FA de bout en bout |
| ~7:00 | Tableau de bord, bouton **Rapport de synthèse** | Indicateurs, export PDF sur la page |
| ~8:00 | Clic sur une province de la mini-carte, puis sur un site | Carte, fiche de site, score écologique |
| ~9:00 | **Suivis › Nouveau suivi** sur une campagne du site, retour à la fiche | Le taux moyen du site change : défi du sujet |
| ~10:00 | **Campagnes** : filtres site + essence + période + survie | Les 4 filtres combinables |
| ~11:00 | Déconnexion, connexion `pierre.ndong@…` | Menus réduits de l'agent (RBAC) |
| ~11:30 | Connexion admin › **Utilisateurs** › créer un compte | Module Utilisateurs, pas d'inscription publique |
| ~12:30 | **Mot de passe oublié** sur le compte créé, code copié depuis la boîte Mailpit | Jeton unique, expiration, invalidation |
| ~13:30 | Diapositives 13 et 14 | Tests, défauts corrigés, perspectives |

---

## 6. Dépannage

| Symptôme | Cause probable | Solution |
|---|---|---|
| « Identifiants incorrects… ou serveur injoignable » avec le bon mot de passe | API arrêtée | Relancer `python manage.py runserver` |
| « Données indisponibles » sur le tableau de bord | API arrêtée ou redémarrée pendant l'affichage | Rouvrir la page |
| Fond de carte absent | Pas de connexion Internet | Choisir le fond **Épuré** : provinces et sites restent affichés |
| Code 2FA refusé | Heure du téléphone décalée, ou secret régénéré par `seed_data` | Synchroniser l'heure ; reconfigurer avec le secret actuel (2.2) |
| Aucun e-mail dans Mailpit | Mailpit n'était pas lancé au moment de la demande | Lancer `mailpit` et redemander un code, ou lire le code dans le terminal de l'API (repli automatique) |
| Connexion bloquée après plusieurs échecs | Limitation à 5 tentatives par minute | Attendre une minute |
| Vidéo absente sur l'écran de connexion | Codec non pris en charge sur le poste | Le panneau illustré s'affiche à la place, sans impact |
