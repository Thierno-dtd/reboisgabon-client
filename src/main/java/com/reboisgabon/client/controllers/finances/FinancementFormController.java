package com.reboisgabon.client.controllers.finances;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.FinancementsApi;
import com.reboisgabon.client.dto.campagnes.Campagne;
import com.reboisgabon.client.dto.finances.Devise;
import com.reboisgabon.client.dto.finances.FinancementRequete;
import com.reboisgabon.client.dto.finances.Partenaire;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.util.CampagneCache;
import com.reboisgabon.client.util.ErreurApiUtil;
import com.reboisgabon.client.util.PartenaireCache;
import com.reboisgabon.client.util.SiteCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class FinancementFormController implements Initializable {

    @FXML private ComboBox<Partenaire> comboPartenaire;
    @FXML private RadioButton radioCampagne;
    @FXML private RadioButton radioSite;
    @FXML private ComboBox<Campagne> comboCampagne;
    @FXML private ComboBox<Site> comboSite;
    @FXML private TextField champMontant;
    @FXML private ComboBox<Devise> comboDevise;
    @FXML private DatePicker champDate;
    @FXML private Label libelleErreur;
    @FXML private Button boutonEnregistrer;

    private final FinancementsApi financementsApi = new FinancementsApi();
    private Runnable onSucces;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboDevise.getItems().setAll(Devise.values());
        PartenaireCache.getInstance().assurerCharge(partenaires -> comboPartenaire.getItems().setAll(partenaires));
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
        SiteCache.getInstance().assurerCharge(sites -> comboSite.getItems().setAll(sites));
        surChangementCible();
    }

    public void definirOnSucces(Runnable onSucces) {
        this.onSucces = onSucces;
    }

    @FXML
    private void surChangementCible() {
        boolean cibleCampagne = radioCampagne.isSelected();
        comboCampagne.setVisible(cibleCampagne);
        comboCampagne.setManaged(cibleCampagne);
        comboSite.setVisible(!cibleCampagne);
        comboSite.setManaged(!cibleCampagne);
    }

    @FXML
    private void enregistrer() {
        if (comboPartenaire.getValue() == null || comboDevise.getValue() == null || champDate.getValue() == null) {
            afficherErreur("Partenaire, devise et date sont obligatoires.");
            return;
        }
        boolean cibleCampagne = radioCampagne.isSelected();
        if (cibleCampagne && comboCampagne.getValue() == null) {
            afficherErreur("Veuillez sélectionner une campagne.");
            return;
        }
        if (!cibleCampagne && comboSite.getValue() == null) {
            afficherErreur("Veuillez sélectionner un site.");
            return;
        }
        FinancementRequete requete = new FinancementRequete();
        requete.setPartenaire(comboPartenaire.getValue().getId());
        requete.setDevise(comboDevise.getValue());
        requete.setDateFinancement(champDate.getValue());
        if (cibleCampagne) {
            requete.setCampagne(comboCampagne.getValue().getId());
        } else {
            requete.setSite(comboSite.getValue().getId());
        }
        try {
            requete.setMontant(new BigDecimal(champMontant.getText()));
        } catch (NumberFormatException e) {
            afficherErreur("Le montant doit être un nombre valide.");
            return;
        }
        masquerErreur();
        boutonEnregistrer.setDisable(true);
        new Thread(() -> {
            try {
                financementsApi.creer(requete);
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
        ((Stage) champMontant.getScene().getWindow()).close();
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