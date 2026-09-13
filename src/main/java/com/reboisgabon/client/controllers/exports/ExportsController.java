package com.reboisgabon.client.controllers.exports;

import com.reboisgabon.client.api.endpoints.ExportsApi;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class ExportsController implements Initializable {

    @FXML private HBox ligneRapportFinancier;
    @FXML private HBox ligneFinancements;

    private final ExportsApi exportsApi = new ExportsApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        boolean peutVoirFinances = SessionManager.getInstance().peutAcceder("finances", "view");
        ligneRapportFinancier.setVisible(peutVoirFinances);
        ligneRapportFinancier.setManaged(peutVoirFinances);
        ligneFinancements.setVisible(peutVoirFinances);
        ligneFinancements.setManaged(peutVoirFinances);
    }

    @FXML
    private void exporterRapportSynthese() {
        telecharger(exportsApi::rapportSynthesePdf, "rapport-synthese.pdf", "PDF", "*.pdf");
    }

    @FXML
    private void exporterRapportFinancier() {
        telecharger(exportsApi::rapportFinancierPdf, "rapport-financier.pdf", "PDF", "*.pdf");
    }

    @FXML
    private void exporterSites() {
        telecharger(exportsApi::sitesExcel, "sites.xlsx", "Excel", "*.xlsx");
    }

    @FXML
    private void exporterCampagnes() {
        telecharger(exportsApi::campagnesExcel, "campagnes.xlsx", "Excel", "*.xlsx");
    }

    @FXML
    private void exporterFinancements() {
        telecharger(exportsApi::financementsExcel, "financements.xlsx", "Excel", "*.xlsx");
    }

    private void telecharger(Supplier<byte[]> fournisseur, String nomFichierDefaut, String libelleFiltre, String motifFiltre) {
        new Thread(() -> {
            try {
                byte[] contenu = fournisseur.get();
                Platform.runLater(() -> enregistrer(contenu, nomFichierDefaut, libelleFiltre, motifFiltre));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de générer l'export."));
            }
        }).start();
    }

    private void enregistrer(byte[] contenu, String nomFichierDefaut, String libelleFiltre, String motifFiltre) {
        FileChooser selecteur = new FileChooser();
        selecteur.setInitialFileName(nomFichierDefaut);
        selecteur.getExtensionFilters().add(new FileChooser.ExtensionFilter(libelleFiltre, motifFiltre));
        File fichier = selecteur.showSaveDialog((Stage) ligneRapportFinancier.getScene().getWindow());
        if (fichier == null) {
            return;
        }
        try (FileOutputStream sortie = new FileOutputStream(fichier)) {
            sortie.write(contenu);
            AlertUtil.information("Export réussi", "Le fichier a été enregistré.");
        } catch (Exception e) {
            AlertUtil.erreur("Erreur", "Impossible d'enregistrer le fichier.");
        }
    }
}