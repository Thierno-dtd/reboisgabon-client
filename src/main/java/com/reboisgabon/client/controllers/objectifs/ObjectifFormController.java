package com.reboisgabon.client.controllers.objectifs;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.ObjectifsApi;
import com.reboisgabon.client.api.endpoints.UsersApi;
import com.reboisgabon.client.dto.objectifs.Objectif;
import com.reboisgabon.client.dto.objectifs.ObjectifRequete;
import com.reboisgabon.client.dto.objectifs.Portee;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurSimple;
import com.reboisgabon.client.util.ErreurApiUtil;
import com.reboisgabon.client.util.SiteCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class ObjectifFormController implements Initializable {

    @FXML private Label libelleTitre;
    @FXML private TextField champTitre;
    @FXML private TextArea champDescription;
    @FXML private ComboBox<Portee> comboPortee;
    @FXML private VBox conteneurProvince;
    @FXML private TextField champProvince;
    @FXML private VBox conteneurSite;
    @FXML private ComboBox<Site> comboSite;
    @FXML private TextField champPlantsCible;
    @FXML private TextField champTauxVise;
    @FXML private DatePicker champDateDebut;
    @FXML private DatePicker champDateEcheance;
    @FXML private ComboBox<UtilisateurSimple> comboResponsable;
    @FXML private Label libelleErreur;
    @FXML private Button boutonEnregistrer;

    private final ObjectifsApi objectifsApi = new ObjectifsApi();
    private final UsersApi usersApi = new UsersApi();
    private Objectif objectifExistant;
    private Runnable onSucces;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboPortee.getItems().setAll(Portee.values());
        SiteCache.getInstance().assurerCharge(sites -> {
            comboSite.getItems().setAll(sites);
            preselectionnerSiteSiEnAttente();
        });
        chargerResponsables();
        surChangementPortee();
    }

    public void definirObjectifExistant(Objectif objectif) {
        this.objectifExistant = objectif;
        libelleTitre.setText("Modifier l'objectif");
        champTitre.setText(objectif.getTitre());
        champDescription.setText(objectif.getDescription());
        comboPortee.setValue(objectif.getPortee());
        champProvince.setText(objectif.getProvince());
        champPlantsCible.setText(objectif.getNombrePlantsCible() != null ? objectif.getNombrePlantsCible().toString() : "");
        champTauxVise.setText(objectif.getTauxSurvieMinimumVise() != null ? objectif.getTauxSurvieMinimumVise().toString() : "");
        champDateDebut.setValue(objectif.getDateDebut());
        champDateEcheance.setValue(objectif.getDateEcheance());
        surChangementPortee();
        preselectionnerSiteSiEnAttente();
    }

    public void definirOnSucces(Runnable onSucces) {
        this.onSucces = onSucces;
    }

    private void preselectionnerSiteSiEnAttente() {
        if (objectifExistant == null || objectifExistant.getSite() == null) {
            return;
        }
        comboSite.getItems().stream()
                .filter(site -> site.getId().equals(objectifExistant.getSite()))
                .findFirst()
                .ifPresent(comboSite::setValue);
    }

    private void chargerResponsables() {
        new Thread(() -> {
            try {
                var page = usersApi.lister();
                Platform.runLater(() -> {
                    comboResponsable.getItems().setAll(page.getResults());
                    if (objectifExistant != null && objectifExistant.getResponsable() != null) {
                        comboResponsable.getItems().stream()
                                .filter(u -> u.getId().equals(objectifExistant.getResponsable()))
                                .findFirst()
                                .ifPresent(comboResponsable::setValue);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> afficherErreur("Impossible de charger la liste des responsables."));
            }
        }).start();
    }

    @FXML
    private void surChangementPortee() {
        Portee portee = comboPortee.getValue();
        boolean afficherProvince = portee == Portee.PROVINCE;
        boolean afficherSite = portee == Portee.SITE;
        conteneurProvince.setVisible(afficherProvince);
        conteneurProvince.setManaged(afficherProvince);
        conteneurSite.setVisible(afficherSite);
        conteneurSite.setManaged(afficherSite);
    }

    @FXML
    private void enregistrer() {
        if (champTitre.getText() == null || champTitre.getText().isBlank() || comboPortee.getValue() == null) {
            afficherErreur("Le titre et la portée sont obligatoires.");
            return;
        }
        ObjectifRequete requete = new ObjectifRequete();
        requete.setTitre(champTitre.getText());
        requete.setDescription(champDescription.getText());
        requete.setPortee(comboPortee.getValue());
        requete.setDateDebut(champDateDebut.getValue());
        requete.setDateEcheance(champDateEcheance.getValue());
        requete.setResponsable(comboResponsable.getValue() != null ? comboResponsable.getValue().getId() : null);
        if (comboPortee.getValue() == Portee.PROVINCE) {
            requete.setProvince(champProvince.getText());
        } else if (comboPortee.getValue() == Portee.SITE) {
            requete.setSite(comboSite.getValue() != null ? comboSite.getValue().getId() : null);
        }
        try {
            requete.setNombrePlantsCible(Integer.parseInt(champPlantsCible.getText()));
            requete.setTauxSurvieMinimumVise(new BigDecimal(champTauxVise.getText()));
        } catch (NumberFormatException e) {
            afficherErreur("Plants cibles et taux visé doivent être des nombres valides.");
            return;
        }
        masquerErreur();
        boutonEnregistrer.setDisable(true);
        new Thread(() -> {
            try {
                if (objectifExistant == null) {
                    objectifsApi.creer(requete);
                } else {
                    objectifsApi.modifier(objectifExistant.getId(), requete);
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
        ((Stage) champTitre.getScene().getWindow()).close();
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