package com.reboisgabon.client.controllers.sites;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.SitesApi;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.dto.sites.StatutSite;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SitesListController implements Initializable {

    @FXML private TextField champRecherche;
    @FXML private TextField champProvince;
    @FXML private ComboBox<StatutSite> comboStatut;
    @FXML private TextField champTauxSurvieMin;
    @FXML private Button boutonNouveauSite;
    @FXML private TableView<Site> tableSites;
    @FXML private TableColumn<Site, String> colonneNom;
    @FXML private TableColumn<Site, String> colonneLocalite;
    @FXML private TableColumn<Site, String> colonneProvince;
    @FXML private TableColumn<Site, BigDecimal> colonneSuperficie;
    @FXML private TableColumn<Site, StatutSite> colonneStatut;
    @FXML private TableColumn<Site, BigDecimal> colonneTauxSurvie;
    @FXML private TableColumn<Site, Void> colonneActions;
    @FXML private Button boutonPrecedent;
    @FXML private Button boutonSuivant;
    @FXML private Label libelleInfoPagination;

    private final SitesApi sitesApi = new SitesApi();
    private PageDrf<Site> pageCourante;

    private interface FournisseurPage {
        PageDrf<Site> charger() throws Exception;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboStatut.getItems().add(null);
        comboStatut.getItems().addAll(StatutSite.values());
        colonneNom.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        data.getValue().getNom()
                ));

        colonneLocalite.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        data.getValue().getLocalite()
                ));

        colonneProvince.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        data.getValue().getProvince()
                ));

        colonneSuperficie.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(
                        data.getValue().getSuperficieHectares()
                ));

        colonneStatut.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(
                        data.getValue().getStatut()
                ));

        colonneTauxSurvie.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(
                        data.getValue().getTauxSurvieMoyen()
                ));
        construireColonneActions();
        boolean peutCreer = SessionManager.getInstance().peutAcceder("sites", "create");
        boutonNouveauSite.setVisible(peutCreer);
        boutonNouveauSite.setManaged(peutCreer);
        rechercher();
    }

    private void construireColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {

            private final Button boutonModifier = new Button("Modifier");
            private final Button boutonSupprimer = new Button("Supprimer");
            private final HBox conteneur = new HBox(8, boutonModifier, boutonSupprimer);

            {
                boutonModifier.getStyleClass().add("bouton-secondaire");
                boutonSupprimer.getStyleClass().add("bouton-secondaire");
                boutonModifier.setOnAction(evenement -> ouvrirModification(getTableView().getItems().get(getIndex())));
                boutonSupprimer.setOnAction(evenement -> supprimer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                if (vide) {
                    setGraphic(null);
                    return;
                }
                boolean peutModifier = SessionManager.getInstance().peutAcceder("sites", "edit");
                boolean peutSupprimer = SessionManager.getInstance().peutAcceder("sites", "delete");
                boutonModifier.setVisible(peutModifier);
                boutonModifier.setManaged(peutModifier);
                boutonSupprimer.setVisible(peutSupprimer);
                boutonSupprimer.setManaged(peutSupprimer);
                setGraphic(conteneur);
            }
        });
    }

    @FXML
    private void rechercher() {
        Map<String, String> filtres = new HashMap<>();
        filtres.put("search", champRecherche.getText());
        filtres.put("province", champProvince.getText());
        if (comboStatut.getValue() != null) {
            filtres.put("statut", comboStatut.getValue().toString());
        }
        filtres.put("taux_survie_min", champTauxSurvieMin.getText());
        chargerPage(() -> sitesApi.rechercher(filtres));
    }

    @FXML
    private void pagePrecedente() {
        if (pageCourante != null && pageCourante.getPrevious() != null) {
            chargerPage(() -> sitesApi.rechercherUrl(pageCourante.getPrevious()));
        }
    }

    @FXML
    private void pageSuivante() {
        if (pageCourante != null && pageCourante.getNext() != null) {
            chargerPage(() -> sitesApi.rechercherUrl(pageCourante.getNext()));
        }
    }

    private void chargerPage(FournisseurPage fournisseur) {
        new Thread(() -> {
            try {
                PageDrf<Site> resultat = fournisseur.charger();
                Platform.runLater(() -> {
                    pageCourante = resultat;
                    tableSites.getItems().setAll(resultat.getResults());
                    libelleInfoPagination.setText(resultat.getCount() + " résultats");
                    boutonPrecedent.setDisable(resultat.getPrevious() == null);
                    boutonSuivant.setDisable(resultat.getNext() == null);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les sites."));
            }
        }).start();
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<SiteFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/site-form.fxml",
                "Nouveau site",
                controleur -> controleur.definirOnSucces(this::rechercher));
    }

    private void ouvrirModification(Site site) {
        DialogUtil.<SiteFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/site-form.fxml",
                "Modifier le site",
                controleur -> {
                    controleur.definirSiteExistant(site);
                    controleur.definirOnSucces(this::rechercher);
                });
    }

    private void supprimer(Site site) {
        boolean confirme = AlertUtil.confirmation("Confirmation", "Supprimer le site \"" + site.getNom() + "\" ?");
        if (!confirme) {
            return;
        }
        new Thread(() -> {
            try {
                sitesApi.supprimer(site.getId());
                Platform.runLater(this::rechercher);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de joindre le serveur."));
            }
        }).start();
    }
}