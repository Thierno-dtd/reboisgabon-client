package com.reboisgabon.client.controllers.suivis;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.SuivisApi;
import com.reboisgabon.client.dto.campagnes.Campagne;
import com.reboisgabon.client.dto.suivis.Suivi;
import com.reboisgabon.client.dto.suivis.SuiviRequete;
import com.reboisgabon.client.util.CampagneCache;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class SuiviFormController implements Initializable {

    @FXML private Label libelleTitre;
    @FXML private ComboBox<Campagne> comboCampagne;
    @FXML private DatePicker champDateControle;
    @FXML private DatePicker champProchaineDate;
    @FXML private TextField champTauxSurvie;
    @FXML private TextField champNombrePlantsVivants;
    @FXML private TextArea champObservations;
    @FXML private Label libelleErreur;
    @FXML private Button boutonEnregistrer;

    private final SuivisApi suivisApi = new SuivisApi();
    private Suivi suiviExistant;
    private Runnable onSucces;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboCampagne.setConverter(new StringConverter<>() {
            @Override
            public String toString(Campagne campagne) {
                return campagne != null ? CampagneCache.getInstance().libelleDe(campagne.getId()) : "";
            }

            @Override
            public Campagne fromString(String texte) {
                return null;
            }
        });
        CampagneCache.getInstance().assurerCharge(campagnes -> {
            comboCampagne.getItems().setAll(campagnes);
            preselectionnerSiEnAttente();
        });
    }

    public void definirSuiviExistant(Suivi suivi) {
        this.suiviExistant = suivi;
        libelleTitre.setText("Modifier le suivi");
        champDateControle.setValue(suivi.getDateControle());
        champProchaineDate.setValue(suivi.getProchaineDateControle());
        champTauxSurvie.setText(suivi.getTauxSurvie() != null ? suivi.getTauxSurvie().toString() : "");
        champNombrePlantsVivants.setText(suivi.getNombrePlantsVivants() != null ? suivi.getNombrePlantsVivants().toString() : "");
        champObservations.setText(suivi.getObservations());
        preselectionnerSiEnAttente();
    }

    public void definirOnSucces(Runnable onSucces) {
        this.onSucces = onSucces;
    }

    private void preselectionnerSiEnAttente() {
        if (suiviExistant == null) {
            return;
        }
        comboCampagne.getItems().stream()
                .filter(campagne -> campagne.getId().equals(suiviExistant.getCampagne()))
                .findFirst()
                .ifPresent(comboCampagne::setValue);
    }

    @FXML
    private void enregistrer() {
        if (comboCampagne.getValue() == null || champDateControle.getValue() == null) {
            afficherErreur("Campagne et date de contrôle sont obligatoires.");
            return;
        }
        SuiviRequete requete = new SuiviRequete();
        requete.setCampagne(comboCampagne.getValue().getId());
        requete.setDateControle(champDateControle.getValue());
        requete.setProchaineDateControle(champProchaineDate.getValue());
        requete.setObservations(champObservations.getText());
        try {
            requete.setTauxSurvie(new BigDecimal(champTauxSurvie.getText()));
            requete.setNombrePlantsVivants(Integer.parseInt(champNombrePlantsVivants.getText()));
        } catch (NumberFormatException e) {
            afficherErreur("Taux de survie et plants vivants doivent être des nombres valides.");
            return;
        }
        masquerErreur();
        boutonEnregistrer.setDisable(true);
        new Thread(() -> {
            try {
                if (suiviExistant == null) {
                    suivisApi.creer(requete);
                } else {
                    suivisApi.modifier(suiviExistant.getId(), requete);
                }
                Platform.runLater(() -> {
                    if (onSucces != null) {
                        onSucces.run();
                    }
                    fermer();
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonEnregistrer.setDisable(false);
                    afficherErreur(ErreurApiUtil.message(e));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonEnregistrer.setDisable(false);
                    afficherErreur("Impossible de joindre le serveur.");
                });
            }
        }).start();
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        ((Stage) champTauxSurvie.getScene().getWindow()).close();
    }

    private void afficherErreur(String message) {
        libelleErreur.setText(message);
        libelleErreur.setVisible(true);
        libelleErreur.setManaged(true);
    }

    private void masquerErreur() {
        libelleErreur.setVisible(false);
        libelleErreur.setManaged(false);
    }
}