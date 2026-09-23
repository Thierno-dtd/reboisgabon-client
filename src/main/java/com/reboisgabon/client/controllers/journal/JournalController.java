package com.reboisgabon.client.controllers.journal;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.endpoints.JournalApi;
import com.reboisgabon.client.dto.audit.ActionAudit;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.JsonVueUtil;
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
        conteneurTableau.getChildren().setAll(JsonVueUtil.construireTableau(resultat.path("results")));
        int total = resultat.path("count").asInt(0);
        libelleInfoPagination.setText(total + " résultats");
        boutonPrecedent.setDisable(resultat.path("previous").isNull() || resultat.path("previous").isMissingNode());
        boutonSuivant.setDisable(resultat.path("next").isNull() || resultat.path("next").isMissingNode());
    }

    @FXML
    private void exporterCsv() {
        new Thread(() -> {
            try {
                byte[] contenu = journalApi.exporterCsv();
                Platform.runLater(() -> enregistrerFichier(contenu));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible d'exporter le journal."));
            }
        }).start();
    }

    private void enregistrerFichier(byte[] contenu) {
        FileChooser selecteur = new FileChooser();
        selecteur.setInitialFileName("journal-activite.csv");
        selecteur.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File fichier = selecteur.showSaveDialog((Stage) champRecherche.getScene().getWindow());
        if (fichier == null) {
            return;
        }
        try (FileOutputStream sortie = new FileOutputStream(fichier)) {
            sortie.write(contenu);
            AlertUtil.information("Export réussi", "Le journal a été enregistré.");
        } catch (Exception e) {
            AlertUtil.erreur("Erreur", "Impossible d'enregistrer le fichier.");
        }
    }
}