package com.reboisgabon.client.controllers.campagnes;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.CampagnesApi;
import com.reboisgabon.client.dto.campagnes.Campagne;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.essences.Essence;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.ErreurApiUtil;
import com.reboisgabon.client.util.EssenceCache;
import com.reboisgabon.client.util.SiteCache;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import com.reboisgabon.client.util.BoutonIconeUtil;
import com.reboisgabon.client.util.FiltreAutoUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CampagnesListController implements Initializable {

    @FXML private ComboBox<Site> comboSite;
    @FXML private ComboBox<Essence> comboEssence;
    @FXML private DatePicker champDateDebut;
    @FXML private DatePicker champDateFin;
    @FXML private Button boutonNouvelleCampagne;
    @FXML private TableView<Campagne> tableCampagnes;
    @FXML private TableColumn<Campagne, String> colonneSite;
    @FXML private TableColumn<Campagne, String> colonneEssence;
    @FXML private TableColumn<Campagne, String> colonneDatePlantation;
    @FXML private TableColumn<Campagne, Integer> colonneNombrePlants;
    @FXML private TableColumn<Campagne, String> colonneTauxSurvie;
    @FXML private TableColumn<Campagne, Void> colonneActions;
    @FXML private Button boutonPrecedent;
    @FXML private Button boutonSuivant;
    @FXML private Label libelleInfoPagination;

    private final CampagnesApi campagnesApi = new CampagnesApi();
    private PageDrf<Campagne> pageCourante;

    private interface FournisseurPage {
        PageDrf<Campagne> charger() throws Exception;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SiteCache.getInstance().assurerCharge(sites -> {
            comboSite.getItems().add(null);
            comboSite.getItems().addAll(sites);
        });
        EssenceCache.getInstance().assurerCharge(essences -> {
            comboEssence.getItems().add(null);
            comboEssence.getItems().addAll(essences);
        });
        colonneSite.setCellValueFactory(data -> new ReadOnlyStringWrapper(SiteCache.getInstance().nomDe(data.getValue().getSite())));
        colonneEssence.setCellValueFactory(data -> new ReadOnlyStringWrapper(EssenceCache.getInstance().nomDe(data.getValue().getEssence())));
        colonneDatePlantation.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getDatePlantation() != null ? data.getValue().getDatePlantation().toString() : ""));
        colonneNombrePlants.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(
                        data.getValue().getNombrePlants()
                ));
        colonneTauxSurvie.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getTauxSurvieMoyen() != null ? data.getValue().getTauxSurvieMoyen().toString() : "—"));
        construireColonneActions();
        boolean peutCreer = SessionManager.getInstance().peutAcceder("campagnes", "create");
        boutonNouvelleCampagne.setVisible(peutCreer);
        boutonNouvelleCampagne.setManaged(peutCreer);

        FiltreAutoUtil.surValeur(comboSite, this::rechercher);
        FiltreAutoUtil.surValeur(comboEssence, this::rechercher);
        FiltreAutoUtil.surValeur(champDateDebut, this::rechercher);
        FiltreAutoUtil.surValeur(champDateFin, this::rechercher);

        rechercher();
    }

    private void construireColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {

            private final Button boutonModifier = BoutonIconeUtil.creer("✏️", "Modifier");
            private final Button boutonSupprimer = BoutonIconeUtil.creer("🗑️", "Supprimer", "bouton-icone-danger");
            private final HBox conteneur = new HBox(6, boutonModifier, boutonSupprimer);

            {
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
                boolean peutModifier = SessionManager.getInstance().peutAcceder("campagnes", "edit");
                boolean peutSupprimer = SessionManager.getInstance().peutAcceder("campagnes", "delete");
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
        if (comboSite.getValue() != null) {
            filtres.put("site", comboSite.getValue().getId());
        }
        if (comboEssence.getValue() != null) {
            filtres.put("essence", comboEssence.getValue().getId());
        }
        if (champDateDebut.getValue() != null) {
            filtres.put("date_debut", champDateDebut.getValue().toString());
        }
        if (champDateFin.getValue() != null) {
            filtres.put("date_fin", champDateFin.getValue().toString());
        }
        chargerPage(() -> campagnesApi.rechercher(filtres));
    }

    @FXML
    private void pagePrecedente() {
        if (pageCourante != null && pageCourante.getPrevious() != null) {
            chargerPage(() -> campagnesApi.rechercherUrl(pageCourante.getPrevious()));
        }
    }

    @FXML
    private void pageSuivante() {
        if (pageCourante != null && pageCourante.getNext() != null) {
            chargerPage(() -> campagnesApi.rechercherUrl(pageCourante.getNext()));
        }
    }

    private void chargerPage(FournisseurPage fournisseur) {
        new Thread(() -> {
            try {
                PageDrf<Campagne> resultat = fournisseur.charger();
                Platform.runLater(() -> {
                    pageCourante = resultat;
                    tableCampagnes.getItems().setAll(resultat.getResults());
                    libelleInfoPagination.setText(resultat.getCount() + " résultats");
                    boutonPrecedent.setDisable(resultat.getPrevious() == null);
                    boutonSuivant.setDisable(resultat.getNext() == null);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les campagnes."));
            }
        }).start();
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<CampagneFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/campagne-form.fxml",
                "Nouvelle campagne",
                controleur -> controleur.definirOnSucces(this::rechercher));
    }

    private void ouvrirModification(Campagne campagne) {
        DialogUtil.<CampagneFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/campagne-form.fxml",
                "Modifier la campagne",
                controleur -> {
                    controleur.definirCampagneExistante(campagne);
                    controleur.definirOnSucces(this::rechercher);
                });
    }

    private void supprimer(Campagne campagne) {
        boolean confirme = AlertUtil.confirmation("Confirmation", "Supprimer cette campagne ?");
        if (!confirme) {
            return;
        }
        new Thread(() -> {
            try {
                campagnesApi.supprimer(campagne.getId());
                Platform.runLater(this::rechercher);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de joindre le serveur."));
            }
        }).start();
    }
}