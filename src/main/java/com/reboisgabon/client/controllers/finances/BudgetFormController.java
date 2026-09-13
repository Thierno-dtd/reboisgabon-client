package com.reboisgabon.client.controllers.finances;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.BudgetsApi;
import com.reboisgabon.client.dto.campagnes.Campagne;
import com.reboisgabon.client.dto.finances.BudgetCampagneRequete;
import com.reboisgabon.client.util.CampagneCache;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class BudgetFormController implements Initializable {

    @FXML private ComboBox<Campagne> comboCampagne;
    @FXML private TextField champMontantAlloue;
    @FXML private TextField champMontantReel;
    @FXML private Label libelleErreur;
    @FXML private Button boutonEnregistrer;

    private final BudgetsApi budgetsApi = new BudgetsApi();
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
        CampagneCache.getInstance().assurerCharge(campagnes -> comboCampagne.getItems().setAll(campagnes));
    }

    public void definirOnSucces(Runnable onSucces) {
        this.onSucces = onSucces;
    }

    @FXML
    private void enregistrer() {
        if (comboCampagne.getValue() == null) {
            afficherErreur("Veuillez sélectionner une campagne.");
            return;
        }
        BudgetCampagneRequete requete = new BudgetCampagneRequete();
        requete.setCampagne(comboCampagne.getValue().getId());
        try {
            requete.setMontantAlloue(new BigDecimal(champMontantAlloue.getText()));
            if (champMontantReel.getText() != null && !champMontantReel.getText().isBlank()) {
                requete.setMontantReel(new BigDecimal(champMontantReel.getText()));
            }
        } catch (NumberFormatException e) {
            afficherErreur("Les montants doivent être des nombres valides.");
            return;
        }
        masquerErreur();
        boutonEnregistrer.setDisable(true);
        new Thread(() -> {
            try {
                budgetsApi.creer(requete);
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
        ((Stage) champMontantAlloue.getScene().getWindow()).close();
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