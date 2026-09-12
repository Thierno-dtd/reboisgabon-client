package com.reboisgabon.client.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.endpoints.DashboardApi;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.JsonVueUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class DashboardController implements Initializable {

    @FXML private TabPane ongletsDashboard;
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

    private interface FournisseurJson {
        JsonNode charger() throws Exception;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!SessionManager.getInstance().peutAcceder("finances", "view")) {
            ongletsDashboard.getTabs().remove(ongletFinancier);
        }
        ongletsDashboard.getSelectionModel().selectedItemProperty()
                .addListener((observable, ancien, nouveau) -> chargerOngletSiNecessaire(nouveau));
        chargerOngletSiNecessaire(ongletApercu);
    }

    private void chargerOngletSiNecessaire(Tab onglet) {
        if (onglet == null || ongletsCharges.contains(onglet)) {
            return;
        }
        ongletsCharges.add(onglet);
        if (onglet == ongletApercu) {
            charger(defilementApercu, dashboardApi::overview);
        } else if (onglet == ongletSites) {
            chargerSites();
        } else if (onglet == ongletEssences) {
            charger(defilementEssences, dashboardApi::essences);
        } else if (onglet == ongletProvinces) {
            charger(defilementProvinces, dashboardApi::provinces);
        } else if (onglet == ongletEvolution) {
            charger(defilementEvolution, dashboardApi::evolution);
        } else if (onglet == ongletAlertes) {
            chargerAlertes();
        } else if (onglet == ongletResponsables) {
            charger(defilementResponsables, dashboardApi::responsables);
        } else if (onglet == ongletFinancier) {
            charger(defilementFinancier, dashboardApi::financier);
        } else if (onglet == ongletObjectifs) {
            charger(defilementObjectifs, dashboardApi::objectifs);
        } else if (onglet == ongletScores) {
            chargerScores();
        } else if (onglet == ongletCarte) {
            charger(defilementCarte, dashboardApi::carteProvinces);
        } else if (onglet == ongletComparaison) {
            charger(defilementComparaison, () -> dashboardApi.comparaisonPeriode("mois"));
        }
    }

    private void charger(ScrollPane cible, FournisseurJson fournisseur) {
        new Thread(() -> {
            try {
                JsonNode resultat = fournisseur.charger();
                Platform.runLater(() -> cible.setContent(JsonVueUtil.construireVue(resultat)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les données du tableau de bord."));
            }
        }).start();
    }

    private void chargerSites() {
        new Thread(() -> {
            try {
                JsonNode resultat = dashboardApi.sites(1);
                Platform.runLater(() -> {
                    VBox conteneur = new VBox(20);
                    Label titreTop = new Label("Top 5 sites");
                    titreTop.getStyleClass().add("titre-2");
                    conteneur.getChildren().addAll(titreTop, JsonVueUtil.construireTableau(resultat.path("top_5")));
                    Label titreClassement = new Label("Classement complet");
                    titreClassement.getStyleClass().add("titre-2");
                    conteneur.getChildren().add(titreClassement);
                    conteneur.getChildren().add(JsonVueUtil.construireBlocPagine(
                            resultat.path("classement_complet"),
                            page -> dashboardApi.sites(page).path("classement_complet")));
                    defilementSites.setContent(conteneur);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger le classement des sites."));
            }
        }).start();
    }

    private void chargerAlertes() {
        new Thread(() -> {
            try {
                JsonNode resultat = dashboardApi.alertes(1, 1);
                Platform.runLater(() -> {
                    VBox conteneur = new VBox(20);
                    Label titreCritiques = new Label("Campagnes à taux critique");
                    titreCritiques.getStyleClass().add("titre-2");
                    conteneur.getChildren().add(titreCritiques);
                    conteneur.getChildren().add(JsonVueUtil.construireBlocPagine(
                            resultat.path("campagnes_taux_critique"),
                            page -> dashboardApi.alertes(page, 1).path("campagnes_taux_critique")));
                    Label titreSansSuivi = new Label("Campagnes sans suivi récent");
                    titreSansSuivi.getStyleClass().add("titre-2");
                    conteneur.getChildren().add(titreSansSuivi);
                    conteneur.getChildren().add(JsonVueUtil.construireBlocPagine(
                            resultat.path("campagnes_sans_suivi_recent"),
                            page -> dashboardApi.alertes(1, page).path("campagnes_sans_suivi_recent")));
                    defilementAlertes.setContent(conteneur);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les alertes."));
            }
        }).start();
    }

    private void chargerScores() {
        new Thread(() -> {
            try {
                JsonNode resultat = dashboardApi.scoresEcologiques(1);
                Platform.runLater(() -> defilementScores.setContent(
                        JsonVueUtil.construireBlocPagine(resultat, dashboardApi::scoresEcologiques)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les scores écologiques."));
            }
        }).start();
    }
}