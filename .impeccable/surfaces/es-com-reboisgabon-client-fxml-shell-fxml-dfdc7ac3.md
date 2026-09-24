---
version: 1
slug: "es-com-reboisgabon-client-fxml-shell-fxml-dfdc7ac3"
primary_target: "src/main/resources/com/reboisgabon/client/fxml/shell.fxml"
related_targets: []
---

Scope: l'application JavaFX entière (shell, tableau de bord, carte, listes CRUD, formulaires, paramètres, connexion). Mode : Operate.
Audience : coordinateurs de l'administration forestière, agents de terrain, financiers ; démo de soutenance projetée.
Scène physique : bureau de ministère à Libreville en plein jour, et vidéoprojecteur de soutenance. D'où un thème clair.

## Direction contract

THESIS: un atlas forestier doublé d'un herbier. Le territoire gabonais est le support (carte, courbes de niveau, cartouches de légende) et chaque site ou essence est un spécimen catalogué, avec une étiquette normalisée à champs réglés. On refuse l'admin sombre au vert néon et le SaaS générique fait de cartes à coins ronds.
OWN-WORLD: fond papier d'archive vert-blanc #F4F6F1, encre forêt #17301F, forêt #1F5136, canopée #3E8E5A, feuille pâle #DCEAD5, or gabonais #C99A1C (état actif, sélection), bleu océan #2E6A9E (eau, données secondaires), latérite #9A5B34 (risque), rouge #B3412E (critique). Feuilles blanches à filet de 1px et rayon de 6px, étiquettes-spécimen à filets pointillés et clés en petites capitales, hachures pour « planifié », courbes de niveau en filigrane dans la barre latérale forêt #12281B et les bandeaux. Public Sans pour l'UI, Archivo pour les titres et les grands chiffres, Spectral italique pour les noms scientifiques. Icônes Material Design 2 en trait.
STORY: le décideur voit où l'on plante, ce qui survit et ce qui dérive ; il clique sur un site ou une province et lit sa fiche-spécimen ; il exporte depuis la page concernée.
FIRST VIEWPORT: tableau de bord. Bandeau-planche pleine largeur (fond forêt, courbes de niveau, trois feuilles dessinées : okoumé, moabi, padouk) portant le compteur d'arbres plantés en Archivo 44px, avec hectares, survie et sites à côté ; bouton « Rapport de synthèse PDF » en haut à droite. En dessous : carte choroplèthe des provinces selon la survie (2/3) et cartouche des alertes (1/3), puis plantations mensuelles, survie par essence et objectifs.
FORM: fusion de la planche d'herbier (candidat 5, tirée au sort) et de la carte d'aménagement forestier (choix IMPECCABLE, candidat 1) sur décision de l'utilisateur. Clé de tirage : c7afbc06. Apports retenus : quantités en chiffres forts (nixie), rampe typographique franche (metro), cycle de vie en étapes numérotées (origami). Interaction signature : un clic sur un site de la carte ouvre sa fiche-spécimen (étiquette, jauge de survie, décomposition du score, campagnes récentes, sites voisins).
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
