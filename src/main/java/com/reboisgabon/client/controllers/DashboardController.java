package com.reboisgabon.client.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.endpoints.DashboardApi;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.JsonVueUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class DashboardController implements Initializable {

    @FXML private TabPane ongletsDashboard;
    @FXML private ComboBox<String> comboPeriode;

    @FXML private Tab ongletApercu;
    @FXML private Tab ongletSites;
    @FXML private Tab ongletEssences;
    @FXML private Tab ongletProvinces;
    @FXML private Tab ongletEvolution;
    @FXML private Tab ongletAlertes;
    @FXML private Tab ongletResponsables;
    @FXML private Tab ongletFinancier;
    @FXML private Tab ongletObjectifs;
    @FXML private Tab ongletScores;
    @FXML private Tab ongletCarte;
    @FXML private Tab ongletComparaison;

    @FXML private ScrollPane defilementApercu;
    @FXML private ScrollPane defilementSites;
    @FXML private ScrollPane defilementEssences;
    @FXML private ScrollPane defilementProvinces;
    @FXML private ScrollPane defilementEvolution;
    @FXML private ScrollPane defilementAlertes;
    @FXML private ScrollPane defilementResponsables;
    @FXML private ScrollPane defilementFinancier;
    @FXML private ScrollPane defilementObjectifs;
    @FXML private ScrollPane defilementScores;
    @FXML private ScrollPane defilementCarte;
    @FXML private ScrollPane defilementComparaison;

    private final DashboardApi dashboardApi = new DashboardApi();
    private final Set<Tab> ongletsCharges = new HashSet<>();

    private final DecimalFormat formatNombre = new DecimalFormat("#,##0.##");

    private static final Map<String, String> ICONES = new HashMap<>();
    private static final Map<String, String> LIBELLES_COURTS = new HashMap<>();

    static {
        ICONES.put("site", "📍");
        ICONES.put("sites", "📍");
        ICONES.put("campagne", "🌱");
        ICONES.put("campagnes", "🌱");
        ICONES.put("essence", "🌳");
        ICONES.put("essences", "🌳");
        ICONES.put("province", "🗺️");
        ICONES.put("provinces", "🗺️");
        ICONES.put("objectif", "🎯");
        ICONES.put("objectifs", "🎯");
        ICONES.put("taux_survie", "📈");
        ICONES.put("survie", "📈");
        ICONES.put("superficie", "📐");
        ICONES.put("plants", "🌿");
        ICONES.put("plant", "🌿");
        ICONES.put("budget", "💰");
        ICONES.put("financement", "💰");
        ICONES.put("financements", "💰");
        ICONES.put("partenaire", "🤝");
        ICONES.put("partenaires", "🤝");
        ICONES.put("alerte", "⚠️");
        ICONES.put("alertes", "⚠️");
        ICONES.put("score", "🍃");
        ICONES.put("scores", "🍃");
        ICONES.put("utilisateur", "👥");
        ICONES.put("utilisateurs", "👥");
        ICONES.put("responsable", "👤");
        ICONES.put("responsables", "👤");
        ICONES.put("suivi", "📊");
        ICONES.put("suivis", "📊");
        ICONES.put("risque", "🚨");

        LIBELLES_COURTS.put("taux_survie_moyen", "Taux survie moyen");
        LIBELLES_COURTS.put("superficie_totale_hectares", "Superficie totale");
        LIBELLES_COURTS.put("nombre_plants_total", "Plants total");
        LIBELLES_COURTS.put("nombre_sites_actifs", "Sites actifs");
        LIBELLES_COURTS.put("nombre_campagnes_actives", "Campagnes actives");
        LIBELLES_COURTS.put("montant_total_finance", "Total financé");
        LIBELLES_COURTS.put("budget_total_alloue", "Budget alloué");
        LIBELLES_COURTS.put("budget_total_reel", "Budget dépensé");
    }

    private interface FournisseurJson {
        JsonNode charger() throws Exception;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (!SessionManager.getInstance().peutAcceder("finances", "view")) {
            ongletsDashboard.getTabs().remove(ongletFinancier);
        }

        comboPeriode.getItems().addAll("Semaine", "Mois", "Année");
        comboPeriode.setValue("Mois");
        comboPeriode.valueProperty().addListener((observable, ancien, nouveau) -> {
            if (ongletsDashboard.getSelectionModel().getSelectedItem() == ongletComparaison) {
                chargerComparaison();
            }
        });

        ongletsDashboard.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, ancien, nouveau) ->
                        chargerOngletSiNecessaire(nouveau));

        chargerOngletSiNecessaire(ongletApercu);
    }

    private void chargerOngletSiNecessaire(Tab onglet) {

        if (onglet == null || ongletsCharges.contains(onglet)) {
            return;
        }

        ongletsCharges.add(onglet);

        if (onglet == ongletApercu) {
            chargerSynthese();
        } else if (onglet == ongletSites) {
            chargerSites();
        } else if (onglet == ongletEssences) {
            chargerOnglet(defilementEssences, dashboardApi::essences, "Essences");
        } else if (onglet == ongletProvinces) {
            chargerOnglet(defilementProvinces, dashboardApi::provinces, "Provinces");
        } else if (onglet == ongletEvolution) {
            chargerOnglet(defilementEvolution, dashboardApi::evolution, "Évolution");
        } else if (onglet == ongletAlertes) {
            chargerAlertes();
        } else if (onglet == ongletResponsables) {
            chargerOnglet(defilementResponsables, dashboardApi::responsables, "Responsables");
        } else if (onglet == ongletFinancier) {
            chargerOnglet(defilementFinancier, dashboardApi::financier, "Financier");
        } else if (onglet == ongletObjectifs) {
            chargerOnglet(defilementObjectifs, dashboardApi::objectifs, "Objectifs");
        } else if (onglet == ongletScores) {
            chargerScores();
        } else if (onglet == ongletCarte) {
            chargerOnglet(defilementCarte, dashboardApi::carteProvinces, "Carte");
        } else if (onglet == ongletComparaison) {
            chargerComparaison();
        }
    }

    private void chargerComparaison() {

        String type;

        switch (comboPeriode.getValue() == null ? "Mois" : comboPeriode.getValue()) {
            case "Semaine":
                type = "semaine";
                break;
            case "Année":
                type = "annee";
                break;
            default:
                type = "mois";
        }

        chargerOnglet(defilementComparaison, () -> dashboardApi.comparaisonPeriode(type), "Comparaison");
    }

    private void chargerSynthese() {

        new Thread(() -> {

            try {

                JsonNode resultat = dashboardApi.overview();

                Platform.runLater(() ->
                        defilementApercu.setContent(
                                construireDashboardGlobal(resultat)
                        )
                );

            } catch (Exception e) {

                Platform.runLater(() ->
                        afficherErreur(defilementApercu,
                                "Impossible de charger la synthèse du tableau de bord.")
                );
            }

        }, "dashboard-overview").start();
    }

    private void chargerOnglet(
            ScrollPane cible,
            FournisseurJson fournisseur,
            String titre
    ) {

        new Thread(() -> {

            try {

                JsonNode resultat = fournisseur.charger();

                Platform.runLater(() ->
                        cible.setContent(
                                construireVueAnalytique(resultat, titre)
                        )
                );

            } catch (Exception e) {

                Platform.runLater(() ->
                        afficherErreur(
                                cible,
                                "Impossible de charger les données de " + titre + "."
                        )
                );
            }

        }, "dashboard-" + titre.toLowerCase()).start();
    }

    private VBox construireDashboardGlobal(JsonNode resultat) {

        VBox principal = conteneurPrincipal();

        principal.getChildren().add(
                construireEnteteSection(
                        "Vue globale",
                        "Les principaux indicateurs du programme ReboisGabon."
                )
        );

        List<Statistique> statistiques = extraireStatistiques(resultat, 8);

        if (!statistiques.isEmpty()) {
            principal.getChildren().add(
                    construireCartesStatistiques(statistiques)
            );
        }

        VBox contenuGraphique = new VBox(18);

        List<BlocGraphique> graphiques =
                extraireGraphiques(resultat, 6);

        if (!graphiques.isEmpty()) {

            for (int i = 0; i < graphiques.size(); i += 2) {

                HBox ligne = new HBox(18);
                ligne.setFillHeight(true);

                BlocGraphique premier = graphiques.get(i);

                Region vuePremier = construireBlocGraphique(premier);
                HBox.setHgrow(vuePremier, Priority.ALWAYS);

                ligne.getChildren().add(vuePremier);

                if (i + 1 < graphiques.size()) {

                    BlocGraphique second = graphiques.get(i + 1);

                    Region vueSecond = construireBlocGraphique(second);
                    HBox.setHgrow(vueSecond, Priority.ALWAYS);

                    ligne.getChildren().add(vueSecond);

                } else {

                    Region espace = new Region();
                    HBox.setHgrow(espace, Priority.ALWAYS);
                    ligne.getChildren().add(espace);
                }

                contenuGraphique.getChildren().add(ligne);
            }
        }

        if (!contenuGraphique.getChildren().isEmpty()) {
            principal.getChildren().add(contenuGraphique);
        }

        if (resultat != null && resultat.isObject()) {

            VBox details = construireDetailsJson(
                    resultat,
                    "Données détaillées"
            );

            if (!details.getChildren().isEmpty()) {
                principal.getChildren().add(details);
            }
        }

        return principal;
    }

    private VBox construireVueAnalytique(
            JsonNode resultat,
            String titre
    ) {

        VBox principal = conteneurPrincipal();

        principal.getChildren().add(
                construireEnteteSection(
                        titre,
                        "Analyse détaillée des données disponibles."
                )
        );

        List<Statistique> statistiques =
                extraireStatistiques(resultat, 6);

        if (!statistiques.isEmpty()) {
            principal.getChildren().add(
                    construireCartesStatistiques(statistiques)
            );
        }

        List<BlocGraphique> graphiques =
                extraireGraphiques(resultat, 8);

        for (int i = 0; i < graphiques.size(); i += 2) {

            HBox ligne = new HBox(18);
            ligne.setFillHeight(true);

            Region premier =
                    construireBlocGraphique(graphiques.get(i));

            HBox.setHgrow(premier, Priority.ALWAYS);
            ligne.getChildren().add(premier);

            if (i + 1 < graphiques.size()) {

                Region second =
                        construireBlocGraphique(graphiques.get(i + 1));

                HBox.setHgrow(second, Priority.ALWAYS);
                ligne.getChildren().add(second);

            } else {

                Region espace = new Region();
                HBox.setHgrow(espace, Priority.ALWAYS);
                ligne.getChildren().add(espace);
            }

            principal.getChildren().add(ligne);
        }

        VBox details =
                construireDetailsJson(resultat, "Données");

        if (!details.getChildren().isEmpty()) {
            principal.getChildren().add(details);
        }

        return principal;
    }

    private void chargerSites() {

        new Thread(() -> {

            try {

                JsonNode resultat = dashboardApi.sites(1);

                Platform.runLater(() -> {

                    VBox principal = conteneurPrincipal();

                    principal.getChildren().add(
                            construireEnteteSection(
                                    "Performance des sites",
                                    "Classement et performance des sites enregistrés."
                            )
                    );

                    List<Statistique> statistiques =
                            extraireStatistiques(resultat, 4);

                    if (!statistiques.isEmpty()) {
                        principal.getChildren().add(
                                construireCartesStatistiques(statistiques)
                        );
                    }

                    JsonNode top =
                            resultat.path("top_5");

                    if (!top.isMissingNode() && !top.isNull()) {

                        principal.getChildren().add(
                                construireBlocDonnees(
                                        "Top 5 des sites",
                                        top
                                )
                        );
                    }

                    JsonNode classement =
                            resultat.path("classement_complet");

                    if (!classement.isMissingNode()
                            && !classement.isNull()) {

                        Label titre =
                                new Label("Classement complet");

                        titre.getStyleClass().add(
                                "dashboard-section-title"
                        );

                        principal.getChildren().add(titre);

                        principal.getChildren().add(
                                JsonVueUtil.construireBlocPagine(
                                        classement,
                                        page -> dashboardApi
                                                .sites(page)
                                                .path("classement_complet")
                                )
                        );
                    }

                    defilementSites.setContent(principal);
                });

            } catch (Exception e) {

                Platform.runLater(() ->
                        afficherErreur(
                                defilementSites,
                                "Impossible de charger le classement des sites."
                        )
                );
            }

        }, "dashboard-sites").start();
    }

    private void chargerAlertes() {

        new Thread(() -> {

            try {

                JsonNode resultat =
                        dashboardApi.alertes(1, 1);

                Platform.runLater(() -> {

                    VBox principal =
                            conteneurPrincipal();

                    principal.getChildren().add(
                            construireEnteteSection(
                                    "Centre des alertes",
                                    "Situations nécessitant une attention particulière."
                            )
                    );

                    JsonNode critiques =
                            resultat.path("campagnes_taux_critique");

                    JsonNode sansSuivi =
                            resultat.path("campagnes_sans_suivi_recent");

                    if (!critiques.isMissingNode()) {

                        principal.getChildren().add(
                                construireBlocDonnees(
                                        "Campagnes à taux critique",
                                        critiques
                                )
                        );
                    }

                    if (!sansSuivi.isMissingNode()) {

                        principal.getChildren().add(
                                construireBlocDonnees(
                                        "Campagnes sans suivi récent",
                                        sansSuivi
                                )
                        );
                    }

                    defilementAlertes.setContent(principal);
                });

            } catch (Exception e) {

                Platform.runLater(() ->
                        afficherErreur(
                                defilementAlertes,
                                "Impossible de charger les alertes."
                        )
                );
            }

        }, "dashboard-alertes").start();
    }

    private void chargerScores() {

        new Thread(() -> {

            try {

                JsonNode resultat =
                        dashboardApi.scoresEcologiques(1);

                Platform.runLater(() -> {

                    VBox principal =
                            conteneurPrincipal();

                    principal.getChildren().add(
                            construireEnteteSection(
                                    "Scores écologiques",
                                    "Classement écologique des éléments suivis."
                            )
                    );

                    List<Statistique> statistiques =
                            extraireStatistiques(resultat, 5);

                    if (!statistiques.isEmpty()) {

                        principal.getChildren().add(
                                construireCartesStatistiques(statistiques)
                        );
                    }

                    principal.getChildren().add(
                            JsonVueUtil.construireBlocPagine(
                                    resultat,
                                    dashboardApi::scoresEcologiques
                            )
                    );

                    defilementScores.setContent(principal);
                });

            } catch (Exception e) {

                Platform.runLater(() ->
                        afficherErreur(
                                defilementScores,
                                "Impossible de charger les scores écologiques."
                        )
                );
            }

        }, "dashboard-scores").start();
    }

    private VBox conteneurPrincipal() {

        VBox box = new VBox(20);

        box.setPadding(
                new Insets(10, 10, 30, 10)
        );

        box.setFillWidth(true);

        return box;
    }

    private VBox construireEnteteSection(
            String titre,
            String description
    ) {

        VBox box = new VBox(4);

        Label titreLabel =
                new Label(titre);

        titreLabel.getStyleClass().add(
                "dashboard-section-title"
        );

        Label descriptionLabel =
                new Label(description);

        descriptionLabel.getStyleClass().add(
                "dashboard-section-description"
        );

        descriptionLabel.setWrapText(true);

        box.getChildren().addAll(
                titreLabel,
                descriptionLabel
        );

        return box;
    }

    private FlowPane construireCartesStatistiques(
            List<Statistique> statistiques
    ) {

        FlowPane flow =
                new FlowPane();

        flow.setHgap(14);
        flow.setVgap(14);

        for (Statistique statistique : statistiques) {

            VBox carte =
                    new VBox(6);

            carte.setPrefWidth(220);
            carte.setMinWidth(190);
            carte.setPadding(
                    new Insets(18)
            );

            carte.getStyleClass().add(
                    "dashboard-stat-card"
            );

            HBox entete = new HBox(8);
            entete.setAlignment(Pos.CENTER_LEFT);

            Label icone = new Label(iconePour(statistique.nom));
            icone.setStyle("-fx-font-size: 16px;");

            Label libelle =
                    new Label(
                            tronquer(libellePour(statistique.nom), 20)
                    );

            libelle.getStyleClass().add(
                    "dashboard-stat-label"
            );

            libelle.setWrapText(true);

            entete.getChildren().addAll(icone, libelle);

            Tooltip.install(carte, new Tooltip(libellePour(statistique.nom)));

            Label valeur =
                    new Label(
                            formaterNombre(
                                    statistique.valeur
                            )
                    );

            valeur.getStyleClass().add(
                    "dashboard-stat-value"
            );

            valeur.setMaxWidth(
                    Double.MAX_VALUE
            );

            carte.getChildren().addAll(
                    entete,
                    valeur
            );

            flow.getChildren().add(carte);
        }

        return flow;
    }

    private Region construireBlocGraphique(
            BlocGraphique bloc
    ) {

        VBox carte =
                new VBox(12);

        carte.setMinHeight(300);
        carte.setPrefHeight(350);
        carte.setPadding(
                new Insets(18)
        );

        carte.getStyleClass().add(
                "dashboard-chart-card"
        );

        String libelleComplet = libellePour(bloc.nom);

        Label titre =
                new Label(iconePour(bloc.nom) + "  " + tronquer(libelleComplet, 28));

        titre.getStyleClass().add(
                "dashboard-chart-title"
        );

        Tooltip.install(titre, new Tooltip(libelleComplet));

        carte.getChildren().add(titre);

        if (bloc.type == TypeGraphique.PIE) {

            PieChart chart =
                    new PieChart();

            chart.setLegendVisible(true);
            chart.setLabelsVisible(true);
            chart.setAnimated(false);

            for (Point point : bloc.points) {

                chart.getData().add(
                        new PieChart.Data(
                                tronquer(humaniser(point.nom), 16),
                                point.valeur
                        )
                );
            }

            VBox.setVgrow(chart, Priority.ALWAYS);

            carte.getChildren().add(chart);

        } else {

            CategoryAxis axeX =
                    new CategoryAxis();

            NumberAxis axeY =
                    new NumberAxis();

            axeX.setLabel("");
            axeY.setLabel("");
            axeX.setTickLabelRotation(-30);

            BarChart<String, Number> chart =
                    new BarChart<>(
                            axeX,
                            axeY
                    );

            chart.setAnimated(false);
            chart.setLegendVisible(false);
            chart.setCategoryGap(12);
            chart.setBarGap(4);

            XYChart.Series<String, Number> serie =
                    new XYChart.Series<>();

            for (Point point : bloc.points) {

                serie.getData().add(
                        new XYChart.Data<>(
                                tronquer(humaniser(point.nom), 12),
                                point.valeur
                        )
                );
            }

            chart.getData().add(serie);

            VBox.setVgrow(chart, Priority.ALWAYS);

            carte.getChildren().add(chart);
        }

        return carte;
    }

    private List<Statistique> extraireStatistiques(
            JsonNode node,
            int maximum
    ) {

        List<Statistique> resultats =
                new ArrayList<>();

        extraireNombres(
                node,
                "",
                resultats,
                maximum
        );

        return resultats;
    }

    private void extraireNombres(
            JsonNode node,
            String chemin,
            List<Statistique> resultats,
            int maximum
    ) {

        if (node == null
                || resultats.size() >= maximum) {
            return;
        }

        if (node.isNumber()) {

            String nom =
                    chemin.isBlank()
                            ? "Valeur"
                            : chemin;

            resultats.add(
                    new Statistique(
                            nom,
                            node.doubleValue()
                    )
            );

            return;
        }

        if (node.isObject()) {

            Iterator<Map.Entry<String, JsonNode>> champs =
                    node.fields();

            while (champs.hasNext()
                    && resultats.size() < maximum) {

                Map.Entry<String, JsonNode> champ =
                        champs.next();

                String nouveauChemin =
                        chemin.isBlank()
                                ? champ.getKey()
                                : chemin + "." + champ.getKey();

                extraireNombres(
                        champ.getValue(),
                        nouveauChemin,
                        resultats,
                        maximum
                );
            }

            return;
        }

        if (node.isArray()) {

            for (int i = 0;
                 i < node.size()
                         && resultats.size() < maximum;
                 i++) {

                extraireNombres(
                        node.get(i),
                        chemin,
                        resultats,
                        maximum
                );
            }
        }
    }

    private List<BlocGraphique> extraireGraphiques(
            JsonNode node,
            int maximum
    ) {

        List<BlocGraphique> graphiques =
                new ArrayList<>();

        extraireGraphiquesRecursif(
                node,
                "",
                graphiques,
                maximum
        );

        return graphiques;
    }

    private void extraireGraphiquesRecursif(
            JsonNode node,
            String nom,
            List<BlocGraphique> resultats,
            int maximum
    ) {

        if (node == null
                || resultats.size() >= maximum) {
            return;
        }

        if (node.isArray()
                && node.size() >= 2) {

            BlocGraphique graphique =
                    construireGraphiqueDepuisTableau(
                            nom,
                            node
                    );

            if (graphique != null) {
                resultats.add(graphique);
            }

            return;
        }

        if (node.isObject()) {

            Iterator<Map.Entry<String, JsonNode>> champs =
                    node.fields();

            while (champs.hasNext()
                    && resultats.size() < maximum) {

                Map.Entry<String, JsonNode> champ =
                        champs.next();

                extraireGraphiquesRecursif(
                        champ.getValue(),
                        champ.getKey(),
                        resultats,
                        maximum
                );
            }
        }
    }

    private BlocGraphique construireGraphiqueDepuisTableau(
            String nom,
            JsonNode tableau
    ) {

        if (tableau == null
                || !tableau.isArray()
                || tableau.size() < 2) {
            return null;
        }

        List<Point> points =
                new ArrayList<>();

        String champLibelle = null;
        String champValeur = null;

        JsonNode premier =
                tableau.get(0);

        if (premier.isObject()) {

            Iterator<Map.Entry<String, JsonNode>> champs =
                    premier.fields();

            while (champs.hasNext()) {

                Map.Entry<String, JsonNode> champ =
                        champs.next();

                if (champValeur == null
                        && champ.getValue().isNumber()) {

                    champValeur = champ.getKey();
                }

                if (champLibelle == null
                        && champ.getValue().isTextual()) {

                    champLibelle = champ.getKey();
                }
            }

            if (champValeur == null) {
                return null;
            }

            for (JsonNode element : tableau) {

                if (!element.isObject()) {
                    continue;
                }

                JsonNode valeur =
                        element.path(champValeur);

                if (!valeur.isNumber()) {
                    continue;
                }

                String libelle;

                if (champLibelle != null
                        && element.has(champLibelle)) {

                    libelle =
                            element.path(champLibelle).asText();

                } else {

                    libelle =
                            "Élément " + (points.size() + 1);
                }

                points.add(
                        new Point(
                                libelle,
                                valeur.doubleValue()
                        )
                );
            }

        } else if (premier.isNumber()) {

            for (int i = 0;
                 i < tableau.size();
                 i++) {

                JsonNode valeur =
                        tableau.get(i);

                if (valeur.isNumber()) {

                    points.add(
                            new Point(
                                    String.valueOf(i + 1),
                                    valeur.doubleValue()
                            )
                    );
                }
            }
        }

        if (points.size() < 2) {
            return null;
        }

        if (points.size() > 10) {
            points =
                    new ArrayList<>(
                            points.subList(0, 10)
                    );
        }

        TypeGraphique type =
                points.size() <= 6
                        ? TypeGraphique.PIE
                        : TypeGraphique.BAR;

        return new BlocGraphique(
                nom.isBlank()
                        ? "Répartition"
                        : nom,
                points,
                type
        );
    }

    private VBox construireDetailsJson(
            JsonNode node,
            String titre
    ) {

        VBox principal =
                new VBox(10);

        Label label =
                new Label(titre);

        label.getStyleClass().add(
                "dashboard-section-title"
        );

        principal.getChildren().add(label);

        if (node == null
                || node.isNull()
                || node.isMissingNode()) {
            return principal;
        }

        if (node.isObject()) {

            GridPane grille =
                    new GridPane();

            grille.setHgap(10);
            grille.setVgap(8);

            int ligne = 0;

            Iterator<Map.Entry<String, JsonNode>> champs =
                    node.fields();

            while (champs.hasNext()) {

                Map.Entry<String, JsonNode> champ =
                        champs.next();

                if (champ.getValue().isArray()
                        || champ.getValue().isObject()) {
                    continue;
                }

                Label cle =
                        new Label(
                                humaniser(
                                        champ.getKey()
                                )
                        );

                cle.getStyleClass().add(
                        "dashboard-detail-key"
                );

                Label valeur =
                        new Label(
                                afficherValeur(
                                        champ.getValue()
                                )
                        );

                valeur.getStyleClass().add(
                        "dashboard-detail-value"
                );

                valeur.setWrapText(true);

                grille.add(
                        cle,
                        0,
                        ligne
                );

                grille.add(
                        valeur,
                        1,
                        ligne
                );

                ligne++;
            }

            if (ligne > 0) {
                principal.getChildren().add(grille);
            }
        }

        return principal;
    }

    private VBox construireBlocDonnees(
            String titre,
            JsonNode donnees
    ) {

        VBox bloc =
                new VBox(12);

        bloc.getStyleClass().add(
                "dashboard-data-card"
        );

        Label titreLabel =
                new Label(titre);

        titreLabel.getStyleClass().add(
                "dashboard-card-title"
        );

        bloc.getChildren().add(titreLabel);

        if (donnees == null
                || donnees.isNull()
                || donnees.isMissingNode()) {

            Label vide =
                    new Label("Aucune donnée disponible.");

            vide.getStyleClass().add(
                    "dashboard-empty"
            );

            bloc.getChildren().add(vide);

            return bloc;
        }

        if (donnees.isArray()) {

            for (JsonNode element : donnees) {

                bloc.getChildren().add(
                        construireLigneDonnee(element)
                );
            }

        } else if (donnees.isObject()) {

            bloc.getChildren().add(
                    construireLigneDonnee(donnees)
            );

        } else {

            Label valeur =
                    new Label(
                            afficherValeur(donnees)
                    );

            valeur.getStyleClass().add(
                    "dashboard-detail-value"
            );

            bloc.getChildren().add(valeur);
        }

        return bloc;
    }

    private Region construireLigneDonnee(
            JsonNode element
    ) {

        if (!element.isObject()) {

            Label valeur =
                    new Label(
                            afficherValeur(element)
                    );

            valeur.getStyleClass().add(
                    "dashboard-list-value"
            );

            return valeur;
        }

        HBox ligne =
                new HBox(14);

        ligne.setAlignment(
                Pos.CENTER_LEFT
        );

        ligne.setPadding(
                new Insets(12)
        );

        ligne.getStyleClass().add(
                "dashboard-list-row"
        );

        Iterator<Map.Entry<String, JsonNode>> champs =
                element.fields();

        int compteur = 0;

        while (champs.hasNext()
                && compteur < 4) {

            Map.Entry<String, JsonNode> champ =
                    champs.next();

            if (champ.getValue().isObject()
                    || champ.getValue().isArray()) {
                continue;
            }

            VBox colonne =
                    new VBox(2);

            HBox.setHgrow(
                    colonne,
                    Priority.ALWAYS
            );

            Label cle =
                    new Label(
                            humaniser(
                                    champ.getKey()
                            )
                    );

            cle.getStyleClass().add(
                    "dashboard-list-key"
            );

            Label valeur =
                    new Label(
                            afficherValeur(
                                    champ.getValue()
                            )
                    );

            valeur.getStyleClass().add(
                    "dashboard-list-value"
            );

            valeur.setWrapText(true);

            colonne.getChildren().addAll(
                    cle,
                    valeur
            );

            ligne.getChildren().add(
                    colonne
            );

            compteur++;
        }

        return ligne;
    }

    private String afficherValeur(
            JsonNode valeur
    ) {

        if (valeur == null
                || valeur.isNull()
                || valeur.isMissingNode()) {
            return "—";
        }

        if (valeur.isNumber()) {
            return formaterNombre(
                    valeur.doubleValue()
            );
        }

        if (valeur.isBoolean()) {
            return valeur.asBoolean()
                    ? "Oui"
                    : "Non";
        }

        return valeur.asText();
    }

    private String formaterNombre(
            double nombre
    ) {

        if (nombre == Math.rint(nombre)) {
            return formatNombre.format(
                    (long) nombre
            );
        }

        return formatNombre.format(nombre);
    }

    private String dernierSegment(String chemin) {
        if (chemin == null || chemin.isBlank()) {
            return "";
        }
        String[] parties = chemin.split("\\.");
        return parties[parties.length - 1];
    }

    private String iconePour(String chemin) {
        String cle = dernierSegment(chemin).toLowerCase();
        for (Map.Entry<String, String> entree : ICONES.entrySet()) {
            if (cle.contains(entree.getKey())) {
                return entree.getValue();
            }
        }
        return "📊";
    }

    private String libellePour(String chemin) {
        String cle = dernierSegment(chemin).toLowerCase();
        if (LIBELLES_COURTS.containsKey(cle)) {
            return LIBELLES_COURTS.get(cle);
        }
        return humaniser(chemin);
    }

    private String tronquer(String valeur, int maximum) {
        if (valeur == null) {
            return "";
        }
        if (valeur.length() <= maximum) {
            return valeur;
        }
        return valeur.substring(0, maximum - 1).trim() + "…";
    }

    private String humaniser(
            String valeur
    ) {

        if (valeur == null
                || valeur.isBlank()) {
            return "Données";
        }

        String dernier = dernierSegment(valeur);

        String resultat =
                dernier
                        .replace(".", " ")
                        .replace("_", " ")
                        .replace("-", " ");

        StringBuilder builder =
                new StringBuilder();

        for (int i = 0;
             i < resultat.length();
             i++) {

            char caractere =
                    resultat.charAt(i);

            if (i > 0
                    && Character.isUpperCase(caractere)
                    && Character.isLowerCase(
                    resultat.charAt(i - 1))) {

                builder.append(' ');
            }

            builder.append(caractere);
        }

        resultat =
                builder.toString()
                        .trim()
                        .toLowerCase();

        if (!resultat.isEmpty()) {

            resultat =
                    Character.toUpperCase(
                            resultat.charAt(0)
                    )
                            + resultat.substring(1);
        }

        return resultat;
    }

    private void afficherErreur(
            ScrollPane cible,
            String message
    ) {

        VBox box =
                conteneurPrincipal();

        box.setAlignment(
                Pos.CENTER
        );

        Label titre =
                new Label("Impossible de charger les données");

        titre.getStyleClass().add(
                "dashboard-error-title"
        );

        Label detail =
                new Label(message);

        detail.getStyleClass().add(
                "dashboard-error-text"
        );

        detail.setWrapText(true);

        box.getChildren().addAll(
                titre,
                detail
        );

        cible.setContent(box);

        AlertUtil.erreur(
                "Erreur",
                message
        );
    }

    private static class Statistique {

        private final String nom;
        private final double valeur;

        private Statistique(
                String nom,
                double valeur
        ) {

            this.nom = nom;
            this.valeur = valeur;
        }
    }

    private static class Point {

        private final String nom;
        private final double valeur;

        private Point(
                String nom,
                double valeur
        ) {

            this.nom = nom;
            this.valeur = valeur;
        }
    }

    private static class BlocGraphique {

        private final String nom;
        private final List<Point> points;
        private final TypeGraphique type;

        private BlocGraphique(
                String nom,
                List<Point> points,
                TypeGraphique type
        ) {

            this.nom = nom;
            this.points = points;
            this.type = type;
        }
    }

    private enum TypeGraphique {
        BAR,
        PIE
    }
}