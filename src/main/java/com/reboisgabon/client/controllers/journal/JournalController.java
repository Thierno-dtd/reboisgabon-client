package com.reboisgabon.client.controllers.journal;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.endpoints.JournalApi;
import com.reboisgabon.client.dto.audit.ActionAudit;
import com.reboisgabon.client.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import com.reboisgabon.client.util.FiltreAutoUtil;

public class JournalController implements Initializable {

    @FXML private TextField champRecherche;
    @FXML private ComboBox<ActionAudit> comboAction;
    @FXML private TextField champModele;
    @FXML private DatePicker champDateDebut;
    @FXML private DatePicker champDateFin;
    @FXML private VBox conteneurTableau;
    @FXML private Button boutonPrecedent;
    @FXML private Button boutonSuivant;
    @FXML private Label libelleInfoPagination;

    private final JournalApi journalApi = new JournalApi();
    private JsonNode pageCourante;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboAction.getItems().add(null);
        comboAction.getItems().addAll(ActionAudit.values());

        FiltreAutoUtil.surSaisie(champRecherche, this::rechercher);
        FiltreAutoUtil.surSaisie(champModele, this::rechercher);
        FiltreAutoUtil.surValeur(comboAction, this::rechercher);
        FiltreAutoUtil.surValeur(champDateDebut, this::rechercher);
        FiltreAutoUtil.surValeur(champDateFin, this::rechercher);

        rechercher();
    }

    @FXML
    private void rechercher() {
        Map<String, String> filtres = new HashMap<>();
        filtres.put("search", champRecherche.getText());
        filtres.put("modele", champModele.getText());
        if (comboAction.getValue() != null) {
            filtres.put("action", comboAction.getValue().toString());
        }
        if (champDateDebut.getValue() != null) {
            filtres.put("date_debut", champDateDebut.getValue().toString());
        }
        if (champDateFin.getValue() != null) {
            filtres.put("date_fin", champDateFin.getValue().toString());
        }
        new Thread(() -> {
            try {
                JsonNode resultat = journalApi.rechercher(filtres);
                Platform.runLater(() -> afficherPage(resultat));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger le journal."));
            }
        }).start();
    }

    @FXML
    private void pagePrecedente() {
        naviguer(pageCourante.path("previous").asText(null));
    }

    @FXML
    private void pageSuivante() {
        naviguer(pageCourante.path("next").asText(null));
    }

    private void naviguer(String urlAbsolue) {
        if (urlAbsolue == null || urlAbsolue.isBlank()) {
            return;
        }
        new Thread(() -> {
            try {
                JsonNode resultat = journalApi.rechercherUrl(urlAbsolue);
                Platform.runLater(() -> afficherPage(resultat));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger la page."));
            }
        }).start();
    }

    private void afficherPage(JsonNode resultat) {
        pageCourante = resultat;
        javafx.scene.layout.VBox feuille = new javafx.scene.layout.VBox();
        feuille.getStyleClass().add("feuille");
        for (JsonNode entree : resultat.path("results")) {
            feuille.getChildren().add(ligne(entree));
        }
        if (feuille.getChildren().isEmpty()) {
            feuille.getChildren().add(com.reboisgabon.client.ui.Composants.etatVide("Aucune activité", "Aucune action ne correspond à ces filtres."));
        }
        conteneurTableau.getChildren().setAll(feuille);
        int total = resultat.path("count").asInt(0);
        libelleInfoPagination.setText(total + " action" + (total > 1 ? "s" : "") + " enregistrée" + (total > 1 ? "s" : ""));
        boutonPrecedent.setDisable(resultat.path("previous").isNull() || resultat.path("previous").isMissingNode());
        boutonSuivant.setDisable(resultat.path("next").isNull() || resultat.path("next").isMissingNode());
    }

    private javafx.scene.layout.HBox ligne(JsonNode e) {
        String action = e.path("action").asText();
        String[] style = switch (action) {
            case "CREATION" -> new String[]{"Création", "foret", com.reboisgabon.client.ui.Icones.AJOUTER};
            case "MODIFICATION" -> new String[]{"Modification", "ocean", com.reboisgabon.client.ui.Icones.MODIFIER};
            case "SUPPRESSION" -> new String[]{"Suppression", "rouge", com.reboisgabon.client.ui.Icones.SUPPRIMER};
            case "CONNEXION" -> new String[]{"Connexion", "neutre", "mdi2l-login"};
            case "CONNEXION_ECHOUEE" -> new String[]{"Échec de connexion", "laterite", com.reboisgabon.client.ui.Icones.ALERTE};
            case "DESACTIVATION" -> new String[]{"Désactivation", "laterite", com.reboisgabon.client.ui.Icones.DESACTIVER};
            default -> new String[]{action, "neutre", com.reboisgabon.client.ui.Icones.HORLOGE};
        };
        javafx.scene.layout.StackPane pastille = new javafx.scene.layout.StackPane(com.reboisgabon.client.ui.Icones.icone(style[2], 17, javafx.scene.paint.Color.web("#46594B")));
        pastille.setMinSize(34, 34);
        pastille.setMaxSize(34, 34);
        pastille.setStyle("-fx-background-color: #F1F4EE; -fx-background-radius: 17;");
        String auteur = e.path("utilisateur_nom").asText("");
        if (auteur.isBlank()) {
            auteur = e.path("utilisateur_email").asText("Système");
        }
        Label phrase = new Label(auteur + "  —  " + libelleModele(e.path("modele").asText()) + " « " + e.path("objet_repr").asText("") + " »");
        phrase.getStyleClass().add("titre-3");
        phrase.setWrapText(true);
        String date = "";
        try {
            date = java.time.OffsetDateTime.parse(e.path("date_action").asText())
                    .format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy 'à' HH:mm", java.util.Locale.FRANCE));
        } catch (Exception ignore) {
        }
        Label detail = new Label(date + (e.hasNonNull("adresse_ip") ? "  ·  IP " + e.path("adresse_ip").asText() : ""));
        detail.getStyleClass().add("texte-petit");
        javafx.scene.layout.VBox textes = new javafx.scene.layout.VBox(2, phrase, detail);
        javafx.scene.layout.HBox.setHgrow(textes, javafx.scene.layout.Priority.ALWAYS);
        textes.setMinWidth(0);
        javafx.scene.layout.HBox ligne = new javafx.scene.layout.HBox(12, pastille, textes, com.reboisgabon.client.ui.Composants.pastille(style[0], style[1]));
        ligne.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ligne.getStyleClass().add("ligne-liste");
        return ligne;
    }

    private String libelleModele(String modele) {
        return switch (modele) {
            case "User" -> "Compte";
            case "SiteReboisement" -> "Site";
            case "CampagnePlantation" -> "Campagne";
            case "SuiviCroissance" -> "Suivi";
            case "Essence" -> "Essence";
            case "ObjectifReboisement" -> "Objectif";
            case "Partenaire" -> "Partenaire";
            case "Financement" -> "Financement";
            case "BudgetCampagne" -> "Budget";
            case "PhotoSuivi" -> "Photo";
            default -> modele;
        };
    }

    @FXML
    private void exporterCsv(javafx.event.ActionEvent evenement) {
        com.reboisgabon.client.ui.ExportUtil.csv((javafx.scene.Node) evenement.getSource(), "journal-activite", () -> {
            try {
                return journalApi.exporterCsv();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}