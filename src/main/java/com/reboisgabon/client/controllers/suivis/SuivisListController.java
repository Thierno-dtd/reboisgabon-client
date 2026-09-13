package com.reboisgabon.client.controllers.suivis;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.SuivisApi;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.suivis.Suivi;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.CampagneCache;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SuivisListController implements Initializable {

    @FXML private DatePicker champDateDebut;
    @FXML private DatePicker champDateFin;
    @FXML private TextField champTauxSurvieMin;
    @FXML private TextField champTauxSurvieMax;
    @FXML private Button boutonNouveauSuivi;
    @FXML private TableView<Suivi> tableSuivis;
    @FXML private TableColumn<Suivi, String> colonneCampagne;
    @FXML private TableColumn<Suivi, String> colonneDateControle;
    @FXML private TableColumn<Suivi, String> colonneTauxSurvie;
    @FXML private TableColumn<Suivi, Integer> colonnePlantsVivants;
    @FXML private TableColumn<Suivi, Void> colonneActions;
    @FXML private Button boutonPrecedent;
    @FXML private Button boutonSuivant;
    @FXML private Label libelleInfoPagination;

    private final SuivisApi suivisApi = new SuivisApi();
    private PageDrf<Suivi> pageCourante;

    private interface FournisseurPage {
        PageDrf<Suivi> charger() throws Exception;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CampagneCache.getInstance().assurerCharge(campagnes -> tableSuivis.refresh());

        colonneCampagne.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        CampagneCache.getInstance().libelleDe(data.getValue().getCampagne())
                ));

        colonneDateControle.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getDateControle() != null
                                ? data.getValue().getDateControle().toString()
                                : ""
                ));

        colonneTauxSurvie.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getTauxSurvie() != null
                                ? data.getValue().getTauxSurvie().toString()
                                : "—"
                ));

        colonnePlantsVivants.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getNombrePlantsVivants()
                ));

        construireColonneActions();

        boolean peutCreer = SessionManager.getInstance().peutAcceder("suivis", "create");
        boutonNouveauSuivi.setVisible(peutCreer);
        boutonNouveauSuivi.setManaged(peutCreer);

        rechercher();
    }

    private void construireColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {

            private final Button boutonModifier = new Button("Modifier");
            private final Button boutonPhotos = new Button("Photos");
            private final Button boutonSupprimer = new Button("Supprimer");
            private final HBox conteneur = new HBox(8, boutonModifier, boutonPhotos, boutonSupprimer);

            {
                boutonModifier.getStyleClass().add("bouton-secondaire");
                boutonPhotos.getStyleClass().add("bouton-secondaire");
                boutonSupprimer.getStyleClass().add("bouton-secondaire");
                boutonModifier.setOnAction(evenement -> ouvrirModification(getTableView().getItems().get(getIndex())));
                boutonPhotos.setOnAction(evenement -> ouvrirPhotos(getTableView().getItems().get(getIndex())));
                boutonSupprimer.setOnAction(evenement -> supprimer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                if (vide) {
                    setGraphic(null);
                    return;
                }
                boolean peutModifier = SessionManager.getInstance().peutAcceder("suivis", "edit");
                boolean peutSupprimer = SessionManager.getInstance().peutAcceder("suivis", "delete");
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
        if (champDateDebut.getValue() != null) {
            filtres.put("date_debut", champDateDebut.getValue().toString());
        }
        if (champDateFin.getValue() != null) {
            filtres.put("date_fin", champDateFin.getValue().toString());
        }
        filtres.put("taux_survie_min", champTauxSurvieMin.getText());
        filtres.put("taux_survie_max", champTauxSurvieMax.getText());
        chargerPage(() -> suivisApi.rechercher(filtres));
    }

    @FXML
    private void pagePrecedente() {
        if (pageCourante != null && pageCourante.getPrevious() != null) {
            chargerPage(() -> suivisApi.rechercherUrl(pageCourante.getPrevious()));
        }
    }

    @FXML
    private void pageSuivante() {
        if (pageCourante != null && pageCourante.getNext() != null) {
            chargerPage(() -> suivisApi.rechercherUrl(pageCourante.getNext()));
        }
    }

    private void chargerPage(FournisseurPage fournisseur) {
        new Thread(() -> {
            try {
                PageDrf<Suivi> resultat = fournisseur.charger();
                Platform.runLater(() -> {
                    pageCourante = resultat;
                    tableSuivis.getItems().setAll(resultat.getResults());
                    libelleInfoPagination.setText(resultat.getCount() + " résultats");
                    boutonPrecedent.setDisable(resultat.getPrevious() == null);
                    boutonSuivant.setDisable(resultat.getNext() == null);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les suivis."));
            }
        }).start();
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<SuiviFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/suivi-form.fxml",
                "Nouveau suivi",
                controleur -> controleur.definirOnSucces(this::rechercher));
    }

    private void ouvrirModification(Suivi suivi) {
        DialogUtil.<SuiviFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/suivi-form.fxml",
                "Modifier le suivi",
                controleur -> {
                    controleur.definirSuiviExistant(suivi);
                    controleur.definirOnSucces(this::rechercher);
                });
    }

    private void ouvrirPhotos(Suivi suivi) {
        DialogUtil.<SuiviPhotosController>ouvrirModal(
                "/com/reboisgabon/client/fxml/suivi-photos.fxml",
                "Photos du suivi",
                controleur -> controleur.definirSuiviId(suivi.getId()));
    }

    private void supprimer(Suivi suivi) {
        boolean confirme = AlertUtil.confirmation("Confirmation", "Supprimer ce suivi ?");
        if (!confirme) {
            return;
        }
        new Thread(() -> {
            try {
                suivisApi.supprimer(suivi.getId());
                Platform.runLater(this::rechercher);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de joindre le serveur."));
            }
        }).start();
    }
}