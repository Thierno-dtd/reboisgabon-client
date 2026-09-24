package com.reboisgabon.client.controllers.sites;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.SitesApi;
import com.reboisgabon.client.api.endpoints.UsersApi;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.dto.sites.SiteRequete;
import com.reboisgabon.client.dto.sites.StatutSite;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurSimple;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class SiteFormController implements Initializable {

    @FXML private Label libelleTitre;
    @FXML private TextField champNom;
    @FXML private TextField champLocalite;
    @FXML private javafx.scene.control.ComboBox<String> champProvince;
    @FXML private TextField champSuperficie;
    @FXML private ComboBox<StatutSite> comboStatut;
    @FXML private TextField champLatitude;
    @FXML private TextField champLongitude;
    @FXML private ComboBox<UtilisateurSimple> comboResponsable;
    @FXML private Label libelleErreur;
    @FXML private Button boutonEnregistrer;

    private final SitesApi sitesApi = new SitesApi();
    private final UsersApi usersApi = new UsersApi();
    private Site siteExistant;
    private Runnable onSucces;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        champProvince.getItems().setAll(SitesListController.PROVINCES);
        comboStatut.getItems().setAll(StatutSite.values());
        chargerResponsables();
    }

    public void definirSiteExistant(Site site) {
        this.siteExistant = site;
        libelleTitre.setText("Modifier le site");
        champNom.setText(site.getNom());
        champLocalite.setText(site.getLocalite());
        champProvince.setValue(site.getProvince());
        champSuperficie.setText(site.getSuperficieHectares() != null ? site.getSuperficieHectares().toString() : "");
        comboStatut.setValue(site.getStatut());
        champLatitude.setText(site.getLatitude() != null ? site.getLatitude().toString() : "");
        champLongitude.setText(site.getLongitude() != null ? site.getLongitude().toString() : "");
    }

    public void definirOnSucces(Runnable onSucces) {
        this.onSucces = onSucces;
    }

    private void chargerResponsables() {
        new Thread(() -> {
            try {
                var page = usersApi.lister();
                Platform.runLater(() -> {
                    comboResponsable.getItems().setAll(page.getResults());
                    if (siteExistant != null && siteExistant.getResponsable() != null) {
                        comboResponsable.getItems().stream()
                                .filter(u -> u.getId().equals(siteExistant.getResponsable()))
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
    private void enregistrer() {
        if (champNom.getText() == null || champNom.getText().isBlank()) {
            afficherErreur("Le nom du site est obligatoire.");
            return;
        }
        SiteRequete requete = new SiteRequete();
        requete.setNom(champNom.getText());
        requete.setLocalite(champLocalite.getText());
        requete.setProvince(champProvince.getValue());
        requete.setStatut(comboStatut.getValue());
        requete.setResponsable(comboResponsable.getValue() != null ? comboResponsable.getValue().getId() : null);
        try {
            if (!champSuperficie.getText().isBlank()) {
                requete.setSuperficieHectares(new BigDecimal(champSuperficie.getText()));
            }
            if (!champLatitude.getText().isBlank()) {
                requete.setLatitude(new BigDecimal(champLatitude.getText()));
            }
            if (!champLongitude.getText().isBlank()) {
                requete.setLongitude(new BigDecimal(champLongitude.getText()));
            }
        } catch (NumberFormatException e) {
            afficherErreur("Superficie, latitude et longitude doivent être des nombres valides.");
            return;
        }
        masquerErreur();
        boutonEnregistrer.setDisable(true);
        new Thread(() -> {
            try {
                if (siteExistant == null) {
                    sitesApi.creer(requete);
                } else {
                    sitesApi.modifier(siteExistant.getId(), requete);
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
        ((Stage) champNom.getScene().getWindow()).close();
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