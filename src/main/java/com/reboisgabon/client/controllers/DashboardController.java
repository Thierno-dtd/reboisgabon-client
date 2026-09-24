package com.reboisgabon.client.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.endpoints.DashboardApi;
import com.reboisgabon.client.api.endpoints.ExportsApi;
import com.reboisgabon.client.api.endpoints.GeolocalisationApi;
import com.reboisgabon.client.controllers.sites.CarteController;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.ui.CarteProvinces;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.ExportUtil;
import com.reboisgabon.client.ui.Icones;
import com.reboisgabon.client.ui.Illustrations;
import com.reboisgabon.client.ui.Navigation;
import com.reboisgabon.client.util.JsonMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class DashboardController implements Initializable {

    @FXML private VBox racine;

    private final DashboardApi dashboardApi = new DashboardApi();
    private final ExportsApi exportsApi = new ExportsApi();
    private final GeolocalisationApi geolocalisationApi = new GeolocalisationApi();

    private final VBox zoneBandeau = new VBox();
    private final VBox zoneCarte = new VBox();
    private final VBox zoneAlertes = new VBox();
    private final VBox zoneEvolution = new VBox();
    private final VBox zoneComparaison = new VBox();
    private final VBox zoneEssences = new VBox();
    private final VBox zoneObjectifs = new VBox();
    private final VBox zoneScores = new VBox();
    private final VBox zoneFinances = new VBox();
    private final VBox zoneEquipes = new VBox();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        boolean finances = SessionManager.getInstance().peutAcceder("finances", "view");

        racine.getChildren().addAll(
                zoneBandeau,
                grille(new double[]{61, 39}, zoneCarte, zoneAlertes),
                grille(new double[]{61, 39}, zoneEvolution, zoneComparaison),
                grille(new double[]{50, 50}, zoneEssences, zoneObjectifs),
                finances ? grille(new double[]{50, 50}, zoneScores, zoneFinances) : grille(new double[]{100}, zoneScores),
                zoneEquipes
        );

        for (VBox zone : List.of(zoneCarte, zoneAlertes, zoneEvolution, zoneComparaison, zoneEssences, zoneObjectifs, zoneScores, zoneFinances, zoneEquipes)) {
            zone.getChildren().setAll(Composants.section("Chargement…", null, Composants.chargement("Récupération des données…")));
            VBox.setVgrow(zone, Priority.ALWAYS);
        }

        charger(dashboardApi::overview, this::construireBandeau, zoneBandeau);
        charger(this::chargerDonneesCarte, this::construireCarte, zoneCarte);
        charger(() -> dashboardApi.alertes(1, 1), this::construireAlertes, zoneAlertes);
        charger(dashboardApi::evolution, this::construireEvolution, zoneEvolution);
        chargerComparaison("mois");
        charger(dashboardApi::essences, this::construireEssences, zoneEssences);
        charger(dashboardApi::objectifs, this::construireObjectifs, zoneObjectifs);
        charger(this::chargerDonneesScores, this::construireScores, zoneScores);
        if (finances) {
            charger(dashboardApi::financier, this::construireFinances, zoneFinances);
        }
        charger(dashboardApi::responsables, this::construireEquipes, zoneEquipes);
    }

    private GridPane grille(double[] pourcentages, Node... enfants) {
        GridPane grille = new GridPane();
        grille.setHgap(18);
        for (double p : pourcentages) {
            ColumnConstraints colonne = new ColumnConstraints();
            colonne.setPercentWidth(p);
            colonne.setHgrow(Priority.ALWAYS);
            colonne.setFillWidth(true);
            grille.getColumnConstraints().add(colonne);
        }
        for (int i = 0; i < enfants.length; i++) {
            grille.add(enfants[i], i, 0);
            GridPane.setFillHeight(enfants[i], true);
        }
        return grille;
    }

    private void charger(Callable<JsonNode> source, Consumer<JsonNode> rendu, VBox zone) {
        Thread tache = new Thread(() -> {
            try {
                JsonNode donnees = source.call();
                Platform.runLater(() -> {
                    try {
                        rendu.accept(donnees);
                    } catch (Exception e) {
                        zone.getChildren().setAll(erreur());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> zone.getChildren().setAll(erreur()));
            }
        });
        tache.setDaemon(true);
        tache.start();
    }

    private Node erreur() {
        Label message = new Label("Ces données n'ont pas pu être chargées. Vérifiez que l'API est joignable.");
        message.getStyleClass().add("message-erreur");
        message.setWrapText(true);
        return Composants.section("Données indisponibles", null, message);
    }

    private double nombre(JsonNode n, String champ) {
        return n != null && n.hasNonNull(champ) ? n.get(champ).asDouble() : 0;
    }

    private Double nombreOuNull(JsonNode n, String champ) {
        return n != null && n.hasNonNull(champ) ? n.get(champ).asDouble() : null;
    }

    private String texte(JsonNode n, String champ) {
        return n != null && n.hasNonNull(champ) ? n.get(champ).asText() : "";
    }

    private void construireBandeau(JsonNode o) {
        StackPane bandeau = new StackPane();
        bandeau.getStyleClass().add("bandeau-planche");
        bandeau.setMinHeight(212);
        Rectangle clip = new Rectangle();
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        clip.widthProperty().bind(bandeau.widthProperty());
        clip.heightProperty().bind(bandeau.heightProperty());
        bandeau.setClip(clip);

        Illustrations.CourbesNiveau courbes = new Illustrations.CourbesNiveau(Color.web("#DCEAD5"), 0.13, 21);
        Illustrations.Canopee canopee = new Illustrations.Canopee(Color.web("#173F2A"), 64, 8);
        VBox decor = new VBox(courbes, canopee);
        VBox.setVgrow(courbes, Priority.ALWAYS);
        decor.setMouseTransparent(true);

        Color traitClair = Color.web("#DCEAD5");
        Color remplissage = Color.web("#2A6446");
        HBox feuilles = new HBox(-6,
                Illustrations.feuille(Illustrations.Espece.PADOUK, 150, traitClair, remplissage),
                Illustrations.feuille(Illustrations.Espece.OKOUME, 188, traitClair, remplissage),
                Illustrations.feuille(Illustrations.Espece.MOABI, 138, traitClair, remplissage));
        feuilles.setAlignment(Pos.BOTTOM_RIGHT);
        feuilles.setMouseTransparent(true);
        feuilles.setPadding(new Insets(0, 26, 6, 0));
        feuilles.setOpacity(0.9);

        Label titre = new Label("Programme national de reboisement");
        titre.getStyleClass().add("titre-bandeau");
        Label sousTitre = new Label("Situation au " + LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRANCE))
                + "  ·  " + (int) nombre(o, "total_campagnes") + " campagnes  ·  " + (int) nombre(o, "total_essences_utilisees") + " essences plantées");
        sousTitre.getStyleClass().add("libelle-bandeau");

        Label compteur = new Label(Composants.nombre(nombre(o, "total_plants_plantes")));
        compteur.getStyleClass().add("chiffre-geant");
        Label compteurLibelle = new Label("arbres plantés sur le territoire");
        compteurLibelle.getStyleClass().add("libelle-bandeau");
        VBox blocCompteur = new VBox(-2, compteur, compteurLibelle);

        HBox chiffres = new HBox(22,
                chiffreBandeau(Composants.decimal(nombre(o, "superficie_totale_hectares")) + " ha", "surface reboisée"),
                separateur(),
                chiffreBandeau(Composants.pourcentage(nombreOuNull(o, "taux_survie_global")), "taux de survie moyen"),
                separateur(),
                chiffreBandeau(String.valueOf((int) nombre(o, "total_sites")), "sites de reboisement"),
                separateur(),
                chiffreBandeau(Composants.nombre(nombre(o, "total_suivis_effectues")), "contrôles de terrain"));
        chiffres.setAlignment(Pos.CENTER_LEFT);

        Button export = Composants.boutonExportPdf("Rapport de synthèse", () -> {});
        export.setOnAction(e -> ExportUtil.pdf(export, "rapport-synthese-reboisgabon", exportsApi::rapportSynthesePdf));

        VBox textes = new VBox(4, titre, sousTitre);
        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);
        HBox tete = new HBox(textes, espace, export);
        tete.setAlignment(Pos.TOP_LEFT);

        VBox contenu = new VBox(16, tete, blocCompteur, chiffres);
        contenu.setPadding(new Insets(22, 26, 24, 28));
        contenu.setPickOnBounds(false);

        bandeau.getChildren().addAll(decor, feuilles, contenu);
        StackPane.setAlignment(feuilles, Pos.BOTTOM_RIGHT);
        zoneBandeau.getChildren().setAll(bandeau);
    }

    private VBox chiffreBandeau(String valeur, String libelle) {
        Label v = new Label(valeur);
        v.getStyleClass().add("chiffre-bandeau");
        Label l = new Label(libelle);
        l.getStyleClass().add("libelle-bandeau");
        return new VBox(1, v, l);
    }

    private Region separateur() {
        Region r = new Region();
        r.getStyleClass().add("separateur-bandeau");
        r.setMinHeight(36);
        return r;
    }

    private JsonNode chargerDonneesCarte() throws Exception {
        var noeud = JsonMapper.instance().createObjectNode();
        noeud.set("provinces", dashboardApi.provinces());
        noeud.set("sites", JsonMapper.instance().readTree(geolocalisationApi.sitesGeojson()));
        return noeud;
    }

    private void construireCarte(JsonNode donnees) {
        CarteProvinces carte = new CarteProvinces();
        carte.setPrefHeight(360);
        List<CarteProvinces.Statistique> stats = new ArrayList<>();
        for (JsonNode p : donnees.get("provinces")) {
            stats.add(new CarteProvinces.Statistique(texte(p, "province"), (int) nombre(p, "nb_sites"),
                    nombre(p, "superficie_totale"), (long) nombre(p, "total_plants"), nombreOuNull(p, "taux_survie_moyen")));
        }
        carte.appliquerStatistiques(stats);
        List<CarteProvinces.Point> points = new ArrayList<>();
        for (JsonNode f : donnees.path("sites").path("features")) {
            JsonNode c = f.path("geometry").path("coordinates");
            JsonNode pr = f.path("properties");
            if (c.size() == 2) {
                points.add(new CarteProvinces.Point(c.get(1).asDouble(), c.get(0).asDouble(), texte(pr, "statut"), texte(pr, "nom")));
            }
        }
        carte.afficherSites(points);
        carte.definirSurClicProvince(nom -> Navigation.<CarteController>aller(Navigation.Ecran.CARTE, c -> c.focaliserProvince(nom)));

        VBox classement = new VBox(0);
        stats.sort((a, b) -> Double.compare(b.survie() == null ? 0 : b.survie(), a.survie() == null ? 0 : a.survie()));
        for (CarteProvinces.Statistique s : stats) {
            Label nom = new Label(s.province());
            nom.getStyleClass().add("texte-corps");
            nom.setMinWidth(118);
            Label detail = new Label(s.sites() + " site" + (s.sites() > 1 ? "s" : ""));
            detail.getStyleClass().add("texte-petit");
            detail.setMinWidth(44);
            HBox ligne = new HBox(8, nom, detail, Composants.barreSurvie(s.survie(), 60));
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.setPadding(new Insets(5, 0, 5, 0));
            classement.getChildren().add(ligne);
        }

        VBox cote = new VBox(10, legende(), classement);
        cote.setMinWidth(260);
        HBox ligneLarge = new HBox(18);
        VBox colonneEtroite = new VBox(14);
        HBox.setHgrow(carte, Priority.ALWAYS);
        StackPane corps = new StackPane();
        Runnable disposer = () -> {
            boolean etroit = corps.getWidth() > 0 && corps.getWidth() < 640;
            if (etroit && carte.getParent() != colonneEtroite) {
                ligneLarge.getChildren().clear();
                colonneEtroite.getChildren().setAll(carte, cote);
                corps.getChildren().setAll(colonneEtroite);
            } else if (!etroit && carte.getParent() != ligneLarge) {
                colonneEtroite.getChildren().clear();
                ligneLarge.getChildren().setAll(carte, cote);
                corps.getChildren().setAll(ligneLarge);
            }
        };
        ligneLarge.getChildren().setAll(carte, cote);
        corps.getChildren().setAll(ligneLarge);
        corps.widthProperty().addListener((o, a, n) -> Platform.runLater(disposer));

        Button voirCarte = Composants.bouton("Ouvrir la carte", Icones.CARTE, "bouton-lien");
        voirCarte.setOnAction(e -> Navigation.aller(Navigation.Ecran.CARTE));
        zoneCarte.getChildren().setAll(Composants.section("Couvert reboisé par province",
                "Couleur : taux de survie moyen. Points : sites. Cliquez une province pour l'explorer.", corps, voirCarte));
    }

    private Node legende() {
        String[][] classes = {{"≥ 90 %", "95"}, {"80 – 90 %", "85"}, {"70 – 80 %", "75"}, {"60 – 70 %", "65"}, {"< 60 %", "50"}};
        HBox ligne = new HBox(8);
        ligne.setAlignment(Pos.CENTER_LEFT);
        for (String[] c : classes) {
            Rectangle carre = new Rectangle(11, 11, Composants.couleurSurvie(Double.parseDouble(c[1])));
            carre.setArcWidth(3);
            carre.setArcHeight(3);
            Label l = new Label(c[0]);
            l.getStyleClass().add("texte-petit");
            HBox item = new HBox(4, carre, l);
            item.setAlignment(Pos.CENTER_LEFT);
            ligne.getChildren().add(item);
        }
        VBox bloc = new VBox(6);
        Label titre = new Label("SURVIE MOYENNE");
        titre.getStyleClass().add("cle-etiquette");
        javafx.scene.layout.FlowPane flux = new javafx.scene.layout.FlowPane(8, 6);
        flux.getChildren().addAll(ligne.getChildren());
        flux.setPrefWrapLength(230);
        bloc.getChildren().addAll(titre, flux);
        return bloc;
    }

    private void construireAlertes(JsonNode a) {
        JsonNode critiques = a.path("campagnes_taux_critique");
        JsonNode sansSuivi = a.path("campagnes_sans_suivi_recent");
        int nbCritiques = critiques.path("count").asInt();
        int nbSansSuivi = sansSuivi.path("count").asInt();

        HBox compteurs = new HBox(12,
                compteurAlerte(String.valueOf(nbCritiques), "sous " + (int) nombre(a, "seuil_critique_pourcent") + " % de survie", nbCritiques > 0 ? "rouge" : "foret"),
                compteurAlerte(String.valueOf(nbSansSuivi), "sans contrôle récent", nbSansSuivi > 0 ? "laterite" : "foret"));

        VBox liste = new VBox();
        List<JsonNode> lignes = new ArrayList<>();
        critiques.path("results").forEach(lignes::add);
        sansSuivi.path("results").forEach(lignes::add);
        int affichees = 0;
        for (JsonNode l : lignes) {
            if (affichees++ >= 5) {
                break;
            }
            boolean critique = l.has("taux_survie_moyen") || l.has("taux_survie");
            Label site = new Label(texte(l, "site"));
            site.getStyleClass().add("titre-3");
            Label detail = new Label(texte(l, "essence") + "  ·  plantée le " + Composants.date(texte(l, "date_plantation")));
            detail.getStyleClass().add("texte-petit");
            VBox textes = new VBox(2, site, detail);
            HBox.setHgrow(textes, Priority.ALWAYS);
            Label badge = critique
                    ? Composants.pastille(Composants.pourcentage(nombreOuNull(l, l.has("taux_survie_moyen") ? "taux_survie_moyen" : "taux_survie")), "rouge")
                    : Composants.pastille((int) nombre(l, "jours_depuis_derniere_activite") + " j sans contrôle", "laterite");
            HBox ligne = new HBox(10, Composants.boutonIcone(critique ? Icones.ALERTE : Icones.HORLOGE, "Alerte", null), textes, badge);
            ligne.getChildren().get(0).setMouseTransparent(true);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.getStyleClass().add("ligne-liste");
            liste.getChildren().add(ligne);
        }
        if (lignes.isEmpty()) {
            liste.getChildren().add(Composants.etatVide("Aucune alerte", "Toutes les campagnes sont suivies et au-dessus du seuil critique."));
        }
        Button voir = Composants.bouton("Planifier les contrôles", Icones.CALENDRIER, "bouton-lien");
        voir.setOnAction(e -> Navigation.aller(Navigation.Ecran.SUIVIS));
        VBox corps = new VBox(12, compteurs, liste, voir);
        zoneAlertes.getChildren().setAll(Composants.section("Alertes terrain", "Ce qui demande une intervention.", corps));
    }

    private VBox compteurAlerte(String valeur, String libelle, String variante) {
        Label v = new Label(valeur);
        v.getStyleClass().add("chiffre-moyen");
        v.setStyle("-fx-text-fill: " + switch (variante) {
            case "rouge" -> "#B3412E";
            case "laterite" -> "#9A5B34";
            default -> "#1F5136";
        } + ";");
        Label l = new Label(libelle);
        l.getStyleClass().add("texte-petit");
        l.setWrapText(true);
        VBox bloc = new VBox(2, v, l);
        bloc.setPadding(new Insets(10, 12, 10, 12));
        bloc.setStyle("-fx-background-color: " + switch (variante) {
            case "rouge" -> "#F8E3DE";
            case "laterite" -> "#F4E6DB";
            default -> "#E4EFDE";
        } + "; -fx-background-radius: 6;");
        HBox.setHgrow(bloc, Priority.ALWAYS);
        bloc.setMaxWidth(Double.MAX_VALUE);
        return bloc;
    }

    private void construireEvolution(JsonNode e) {
        CategoryAxis axeX = new CategoryAxis();
        NumberAxis axeY = new NumberAxis();
        axeY.setTickLabelFormatter(new NumberAxis.DefaultFormatter(axeY) {
            @Override
            public String toString(Number valeur) {
                return valeur.doubleValue() >= 1000 ? Composants.nombre(valeur.doubleValue() / 1000) + " k" : Composants.nombre(valeur);
            }
        });
        BarChart<String, Number> graphique = new BarChart<>(axeX, axeY);
        graphique.setAnimated(false);
        graphique.setLegendVisible(false);
        graphique.setCategoryGap(6);
        graphique.setPrefHeight(270);
        graphique.setMinHeight(240);
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM yy", Locale.FRANCE);
        List<JsonNode> mois = new ArrayList<>();
        e.path("plantations_par_mois").forEach(mois::add);
        int debut = Math.max(0, mois.size() - 18);
        long total = 0;
        for (int i = debut; i < mois.size(); i++) {
            JsonNode m = mois.get(i);
            String libelle = LocalDate.parse(texte(m, "mois")).format(format);
            long plants = (long) nombre(m, "total_plants");
            total += plants;
            XYChart.Data<String, Number> point = new XYChart.Data<>(libelle, plants);
            serie.getData().add(point);
        }
        graphique.getData().add(serie);
        for (XYChart.Data<String, Number> point : serie.getData()) {
            if (point.getNode() != null) {
                Tooltip.install(point.getNode(), new Tooltip(point.getXValue() + " : " + Composants.nombre(point.getYValue()) + " plants"));
            }
        }
        Label resume = new Label(Composants.nombre(total) + " plants mis en terre sur les 18 derniers mois");
        resume.getStyleClass().add("texte-muet");
        VBox corps = new VBox(4, resume, graphique);
        zoneEvolution.getChildren().setAll(Composants.section("Plantations mensuelles", "Nombre de plants mis en terre chaque mois.", corps));
    }

    private void chargerComparaison(String type) {
        charger(() -> dashboardApi.comparaisonPeriode(type), c -> construireComparaison(c, type), zoneComparaison);
    }

    private void construireComparaison(JsonNode c, String type) {
        ToggleGroup groupe = new ToggleGroup();
        ToggleButton mois = new ToggleButton("Mois");
        ToggleButton annee = new ToggleButton("Année");
        mois.setToggleGroup(groupe);
        annee.setToggleGroup(groupe);
        ("annee".equals(type) ? annee : mois).setSelected(true);
        mois.setOnAction(e -> chargerComparaison("mois"));
        annee.setOnAction(e -> chargerComparaison("annee"));
        HBox segment = new HBox(2, mois, annee);
        segment.getStyleClass().add("segment");

        JsonNode actuelle = c.path("periode_actuelle");
        JsonNode precedente = c.path("periode_precedente");
        JsonNode evolution = c.path("evolution_pourcentage");
        String periode = "Du " + Composants.date(texte(actuelle, "debut")) + " au " + Composants.date(texte(actuelle, "fin"))
                + ", comparé à la période précédente";
        Label libellePeriode = new Label(periode);
        libellePeriode.getStyleClass().add("texte-petit");
        libellePeriode.setWrapText(true);

        VBox lignes = new VBox(
                ligneComparaison(Icones.CAMPAGNE, "Campagnes lancées", Composants.nombre(nombre(actuelle, "nombre_campagnes")),
                        Composants.nombre(nombre(precedente, "nombre_campagnes")), nombreOuNull(evolution, "nombre_campagnes")),
                ligneComparaison(Icones.ARBRE, "Plants mis en terre", Composants.nombre(nombre(actuelle, "total_plants")),
                        Composants.nombre(nombre(precedente, "total_plants")), nombreOuNull(evolution, "total_plants")),
                ligneComparaison(Icones.SCORE, "Survie mesurée", Composants.pourcentage(nombreOuNull(actuelle, "taux_survie_moyen")),
                        Composants.pourcentage(nombreOuNull(precedente, "taux_survie_moyen")), nombreOuNull(evolution, "taux_survie_moyen")));
        VBox corps = new VBox(10, libellePeriode, lignes);
        zoneComparaison.getChildren().setAll(Composants.section("Rythme du programme", null, corps, segment));
    }

    private HBox ligneComparaison(String icone, String libelle, String actuelle, String precedente, Double variation) {
        StackPane pastilleIcone = new StackPane(Icones.icone(icone));
        pastilleIcone.getStyleClass().add("icone-page");
        pastilleIcone.setStyle("-fx-min-width: 34; -fx-min-height: 34; -fx-max-width: 34; -fx-max-height: 34;");
        Label l = new Label(libelle);
        l.getStyleClass().add("texte-petit");
        Label v = new Label(actuelle);
        v.getStyleClass().add("chiffre-moyen");
        Label p = new Label("avant : " + precedente);
        p.getStyleClass().add("texte-petit");
        VBox textes = new VBox(0, l, v, p);
        HBox.setHgrow(textes, Priority.ALWAYS);
        Node badge;
        if (variation == null) {
            badge = Composants.pastille("n.d.", "neutre");
        } else {
            Label b = new Label((variation > 0 ? "+" : "") + Composants.decimal(variation) + " %", Icones.icone(variation >= 0 ? Icones.HAUSSE : Icones.BAISSE, 14,
                    Color.web(variation >= 0 ? "#256B3E" : "#B3412E")));
            b.getStyleClass().add(variation >= 0 ? "variation-hausse" : "variation-baisse");
            badge = b;
        }
        HBox ligne = new HBox(12, pastilleIcone, textes, badge);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.getStyleClass().add("ligne-liste");
        return ligne;
    }

    private void construireEssences(JsonNode e) {
        VBox lignes = new VBox();
        int rang = 0;
        for (JsonNode es : e) {
            if (rang++ >= 7) {
                break;
            }
            String nom = texte(es, "essence");
            Node dessin = Illustrations.feuille(Illustrations.especePour(nom), 34, Color.web("#1F5136"), Color.web("#E4EFDE"));
            StackPane vignette = new StackPane(dessin);
            vignette.setMinSize(38, 38);
            vignette.setMaxSize(38, 38);
            vignette.setStyle("-fx-background-color: #F4F7F1; -fx-background-radius: 6;");
            Label libelle = new Label(nom);
            libelle.getStyleClass().add("titre-3");
            Label scientifique = new Label(texte(es, "nom_scientifique"));
            scientifique.getStyleClass().add("nom-scientifique");
            scientifique.setStyle("-fx-font-size: 12px;");
            HBox noms = new HBox(6, libelle);
            if (es.path("croissance_rapide").asBoolean()) {
                noms.getChildren().add(Composants.pastille("rapide", "or"));
            }
            noms.setAlignment(Pos.CENTER_LEFT);
            VBox textes = new VBox(0, noms, scientifique);
            HBox.setHgrow(textes, Priority.ALWAYS);
            Label plants = new Label(Composants.nombre(nombre(es, "total_plants")) + " plants");
            plants.getStyleClass().add("texte-petit");
            plants.setMinWidth(88);
            HBox ligne = new HBox(12, vignette, textes, plants, Composants.barreSurvie(nombreOuNull(es, "taux_survie_moyen"), 80));
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.getStyleClass().add("ligne-liste");
            lignes.getChildren().add(ligne);
        }
        Button voir = Composants.bouton("Toutes les essences", Icones.ESSENCE, "bouton-lien");
        voir.setOnAction(ev -> Navigation.aller(Navigation.Ecran.ESSENCES));
        zoneEssences.getChildren().setAll(Composants.section("Survie par essence", "Les espèces qui reprennent le mieux sur le terrain.", lignes, voir));
    }

    private void construireObjectifs(JsonNode o) {
        HBox resume = new HBox(10,
                compteurAlerte(String.valueOf((int) nombre(o, "total_objectifs_actifs")), "objectifs suivis", "foret"),
                compteurAlerte(String.valueOf((int) nombre(o, "objectifs_atteints")), "atteints", "foret"),
                compteurAlerte(String.valueOf((int) nombre(o, "objectifs_en_retard")), "en retard", nombre(o, "objectifs_en_retard") > 0 ? "rouge" : "foret"));
        VBox lignes = new VBox();
        int n = 0;
        for (JsonNode ob : o.path("objectifs")) {
            if (n++ >= 5) {
                break;
            }
            Label titre = new Label(texte(ob, "titre"));
            titre.getStyleClass().add("titre-3");
            titre.setWrapText(true);
            String statut = texte(ob, "statut_calcule");
            Label pastille = switch (statut) {
                case "ATTEINT" -> Composants.pastille("Atteint", "foret");
                case "NON_ATTEINT" -> Composants.pastille("Non atteint", "rouge");
                case "ANNULE" -> Composants.pastille("Annulé", "neutre");
                default -> Composants.pastille("En cours", "ocean");
            };
            Region espace = new Region();
            HBox.setHgrow(espace, Priority.ALWAYS);
            HBox tete = new HBox(8, titre, espace, pastille);
            tete.setAlignment(Pos.CENTER_LEFT);
            double progression = nombre(ob, "progression_pourcentage");
            Label detail = new Label(Composants.nombre(nombre(ob, "plants_realises")) + " / " + Composants.nombre(nombre(ob, "nombre_plants_cible"))
                    + " plants  ·  échéance " + Composants.date(texte(ob, "date_echeance")));
            detail.getStyleClass().add("texte-petit");
            Label pct = new Label(Composants.decimal(progression) + " %");
            pct.getStyleClass().add("titre-3");
            HBox barre = Composants.barreProgression(progression / 100.0, 260,
                    "NON_ATTEINT".equals(statut) ? Color.web("#B3412E") : progression >= 100 ? Color.web("#1F5136") : Color.web("#C99A1C"));
            HBox ligneBarre = new HBox(10, barre, pct);
            ligneBarre.setAlignment(Pos.CENTER_LEFT);
            VBox bloc = new VBox(5, tete, ligneBarre, detail);
            bloc.getStyleClass().add("ligne-liste");
            lignes.getChildren().add(bloc);
        }
        Button voir = Composants.bouton("Gérer les objectifs", Icones.OBJECTIF, "bouton-lien");
        voir.setOnAction(e -> Navigation.aller(Navigation.Ecran.OBJECTIFS));
        zoneObjectifs.getChildren().setAll(Composants.section("Objectifs de reboisement", "Progression calculée à partir des campagnes réelles.", new VBox(12, resume, lignes), voir));
    }

    private JsonNode chargerDonneesScores() throws Exception {
        var noeud = JsonMapper.instance().createObjectNode();
        noeud.set("scores", dashboardApi.scoresEcologiques(1));
        noeud.set("sites", dashboardApi.sites(1));
        return noeud;
    }

    private void construireScores(JsonNode d) {
        JsonNode scores = d.path("scores");
        Label moyenne = new Label(Composants.decimal(nombre(scores, "score_moyen_national")));
        moyenne.getStyleClass().add("chiffre-fort");
        Label sur = new Label("/ 100  score écologique moyen national");
        sur.getStyleClass().add("unite");
        HBox tete = new HBox(6, moyenne, sur);
        tete.setAlignment(Pos.BASELINE_LEFT);

        VBox meilleurs = new VBox();
        int n = 0;
        for (JsonNode s : scores.path("classement").path("results")) {
            if (n++ >= 4) {
                break;
            }
            meilleurs.getChildren().add(ligneSite(n, texte(s, "nom"), texte(s, "localite"), texte(s, "id"),
                    Composants.pastille(Composants.decimal(nombre(s, "score_global")) + " · " + libelleClasse(texte(s, "classe")), varianteClasse(texte(s, "classe")))));
        }
        VBox risques = new VBox();
        n = 0;
        for (JsonNode s : d.path("sites").path("top_5_sites_a_risque")) {
            if (n++ >= 4) {
                break;
            }
            risques.getChildren().add(ligneSite(n, texte(s, "nom"), texte(s, "localite") + " · " + texte(s, "province"), texte(s, "id"),
                    Composants.barreSurvie(nombreOuNull(s, "taux_survie_moyen"), 50)));
        }
        Label t1 = new Label("MEILLEURS SCORES");
        t1.getStyleClass().add("cle-etiquette");
        Label t2 = new Label("SURVIE LA PLUS FAIBLE");
        t2.getStyleClass().add("cle-etiquette");
        VBox colonne1 = new VBox(6, t1, meilleurs);
        VBox colonne2 = new VBox(6, t2, risques);
        HBox.setHgrow(colonne1, Priority.ALWAYS);
        HBox.setHgrow(colonne2, Priority.ALWAYS);
        colonne1.setPrefWidth(100);
        colonne2.setPrefWidth(100);
        HBox colonnes = new HBox(18, colonne1, colonne2);
        zoneScores.getChildren().setAll(Composants.section("Classement des sites", "Score écologique : survie, régularité des suivis, diversité, statut.", new VBox(12, tete, colonnes)));
    }

    private HBox ligneSite(int rang, String nom, String lieu, String id, Node droite) {
        Label numero = new Label(String.valueOf(rang));
        numero.getStyleClass().add("titre-3");
        numero.setMinWidth(16);
        Label libelle = new Label(nom);
        libelle.getStyleClass().add("titre-3");
        Label detail = new Label(lieu);
        detail.getStyleClass().add("texte-petit");
        VBox textes = new VBox(1, libelle, detail);
        HBox.setHgrow(textes, Priority.ALWAYS);
        textes.setMinWidth(0);
        HBox ligne = new HBox(10, numero, textes, droite);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.getStyleClass().addAll("ligne-liste", "ligne-liste-cliquable");
        ligne.setPadding(new Insets(8, 4, 8, 4));
        ligne.setOnMouseClicked(e -> Navigation.<CarteController>aller(Navigation.Ecran.CARTE, c -> c.focaliserSite(id)));
        Tooltip.install(ligne, new Tooltip("Voir la fiche du site sur la carte"));
        return ligne;
    }

    private String libelleClasse(String classe) {
        return switch (classe) {
            case "EXCELLENT" -> "Excellent";
            case "BON" -> "Bon";
            case "MOYEN" -> "Moyen";
            case "FAIBLE" -> "Faible";
            case "CRITIQUE" -> "Critique";
            default -> classe;
        };
    }

    private String varianteClasse(String classe) {
        return switch (classe) {
            case "EXCELLENT" -> "foret";
            case "BON" -> "canopee";
            case "MOYEN" -> "or";
            case "FAIBLE" -> "laterite";
            default -> "rouge";
        };
    }

    private void construireFinances(JsonNode f) {
        double finance = nombre(f, "total_finance");
        double budget = nombre(f, "budget_total_alloue");
        double cout = nombre(f, "cout_reel_total");

        GridPane chiffres = new GridPane();
        chiffres.setHgap(18);
        chiffres.setVgap(10);
        chiffres.add(chiffreFinance("Fonds mobilisés", Composants.montant(finance)), 0, 0);
        chiffres.add(chiffreFinance("Partenaires actifs", String.valueOf((int) nombre(f, "nombre_partenaires_actifs"))), 1, 0);
        chiffres.add(chiffreFinance("Budget alloué", Composants.montant(budget)), 0, 1);
        chiffres.add(chiffreFinance("Coût par plant survivant", Composants.nombre(nombre(f, "cout_moyen_par_plant_survivant")) + " FCFA"), 1, 1);

        double ratio = budget > 0 ? cout / budget : 0;
        Label consommation = new Label("Dépenses réelles : " + Composants.montant(cout) + " (" + Composants.decimal(ratio * 100) + " % du budget alloué)");
        consommation.getStyleClass().add("texte-petit");
        HBox barre = Composants.barreProgression(Math.min(1, ratio), 420, ratio > 1 ? Color.web("#B3412E") : Color.web("#1F5136"));

        HBox repartition = new HBox();
        repartition.setMinHeight(12);
        repartition.setMaxHeight(12);
        VBox legendeTypes = new VBox(4);
        String[] couleurs = {"#1F5136", "#C99A1C", "#2E6A9E", "#9A5B34", "#7FA88A"};
        int i = 0;
        for (JsonNode t : f.path("financement_par_type_partenaire")) {
            double part = finance > 0 ? nombre(t, "total") / finance : 0;
            Region segment = new Region();
            segment.setStyle("-fx-background-color: " + couleurs[i % couleurs.length] + ";");
            segment.prefWidthProperty().bind(repartition.widthProperty().multiply(part));
            repartition.getChildren().add(segment);
            Circle puce = new Circle(5, Color.web(couleurs[i % couleurs.length]));
            Label l = new Label(libelleTypePartenaire(texte(t, "partenaire__type_partenaire")) + "  —  " + Composants.montant(nombre(t, "total"))
                    + "  (" + Composants.decimal(part * 100) + " %)");
            l.getStyleClass().add("texte-petit");
            HBox item = new HBox(8, puce, l);
            item.setAlignment(Pos.CENTER_LEFT);
            legendeTypes.getChildren().add(item);
            i++;
        }
        Rectangle clip = new Rectangle();
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        clip.widthProperty().bind(repartition.widthProperty());
        clip.heightProperty().bind(repartition.heightProperty());
        repartition.setClip(clip);

        Label t = new Label("ORIGINE DES FONDS");
        t.getStyleClass().add("cle-etiquette");
        Button voir = Composants.bouton("Ouvrir les finances", Icones.FINANCES, "bouton-lien");
        voir.setOnAction(e -> Navigation.aller(Navigation.Ecran.FINANCES));
        VBox corps = new VBox(12, chiffres, new VBox(6, consommation, barre), new VBox(8, t, repartition, legendeTypes));
        zoneFinances.getChildren().setAll(Composants.section("Financement du programme", "Bailleurs, budgets et coûts réels.", corps, voir));
    }

    private VBox chiffreFinance(String libelle, String valeur) {
        Label l = new Label(libelle);
        l.getStyleClass().add("texte-petit");
        Label v = new Label(valeur);
        v.getStyleClass().add("chiffre-moyen");
        return new VBox(1, l, v);
    }

    private String libelleTypePartenaire(String type) {
        return switch (type) {
            case "BAILLEUR_INTL" -> "Bailleurs internationaux";
            case "ONG" -> "ONG";
            case "ENTREPRISE" -> "Entreprises (RSE)";
            case "ETAT" -> "État gabonais";
            default -> type;
        };
    }

    private void construireEquipes(JsonNode r) {
        GridPane grille = new GridPane();
        grille.setHgap(18);
        for (int c = 0; c < 2; c++) {
            ColumnConstraints colonne = new ColumnConstraints();
            colonne.setPercentWidth(50);
            grille.getColumnConstraints().add(colonne);
        }
        int n = 0;
        for (JsonNode a : r) {
            if (n >= 8) {
                break;
            }
            Label avatar = new Label(Composants.initiales(texte(a, "agent")));
            avatar.getStyleClass().add("avatar");
            avatar.setStyle("-fx-background-color: #E4EFDE; -fx-text-fill: #1F5136;");
            Label nom = new Label(texte(a, "agent"));
            nom.getStyleClass().add("titre-3");
            Label detail = new Label((int) nombre(a, "nombre_campagnes") + " campagnes  ·  " + Composants.nombre(nombre(a, "total_plants")) + " plants");
            detail.getStyleClass().add("texte-petit");
            VBox textes = new VBox(1, nom, detail);
            HBox.setHgrow(textes, Priority.ALWAYS);
            HBox ligne = new HBox(10, avatar, textes, Composants.barreSurvie(nombreOuNull(a, "taux_survie_moyen"), 60));
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.getStyleClass().add("ligne-liste");
            grille.add(ligne, n % 2, n / 2);
            n++;
        }
        zoneEquipes.getChildren().setAll(Composants.section("Équipes de terrain", "Responsables de campagnes, par volume planté et survie obtenue.", grille));
    }
}
