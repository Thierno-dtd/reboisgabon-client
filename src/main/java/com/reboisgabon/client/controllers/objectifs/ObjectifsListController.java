package com.reboisgabon.client.controllers.objectifs;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.ObjectifsApi;
import com.reboisgabon.client.dto.objectifs.Objectif;
import com.reboisgabon.client.dto.objectifs.Portee;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
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

public class ObjectifsListController implements Initializable {

    @FXML private ComboBox<Portee> comboPortee;
    @FXML private Button boutonNouvelObjectif;
    @FXML private TableView<Objectif> tableObjectifs;
    @FXML private TableColumn<Objectif, String> colonneTitre;
    @FXML private TableColumn<Objectif, String> colonnePortee;
    @FXML private TableColumn<Objectif, String> colonneEcheance;
    @FXML private TableColumn<Objectif, Double> colonneProgression;
    @FXML private TableColumn<Objectif, String> colonneStatut;
    @FXML private TableColumn<Objectif, Void> colonneActions;

    private final ObjectifsApi objectifsApi = new ObjectifsApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboPortee.getItems().add(null);
        comboPortee.getItems().addAll(Portee.values());

        colonneTitre.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getTitre()));

        colonnePortee.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getPortee() != null
                                ? data.getValue().getPortee().toString()
                                : ""
                ));

        colonneEcheance.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getDateEcheance() != null
                                ? data.getValue().getDateEcheance().toString()
                                : ""
                ));

        colonneStatut.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getStatutCalcule() != null
                                ? data.getValue().getStatutCalcule().toString()
                                : ""
                ));

        construireColonneProgression();
        construireColonneActions();

        boolean peutCreer = SessionManager.getInstance().peutAcceder("objectifs", "create");
        boutonNouvelObjectif.setVisible(peutCreer);
        boutonNouvelObjectif.setManaged(peutCreer);

        FiltreAutoUtil.surValeur(comboPortee, this::rechercher);

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
                boolean peutModifier = SessionManager.getInstance().peutAcceder("objectifs", "edit");
                boolean peutSupprimer = SessionManager.getInstance().peutAcceder("objectifs", "delete");
                boutonModifier.setVisible(peutModifier);
                boutonModifier.setManaged(peutModifier);
                boutonSupprimer.setVisible(peutSupprimer);
                boutonSupprimer.setManaged(peutSupprimer);
                setGraphic(conteneur);
            }
        });
    }

    private void construireColonneProgression() {
        colonneProgression.setCellFactory(colonne -> new TableCell<>() {

            private final ProgressBar barre = new ProgressBar();

            {
                barre.getStyleClass().add("barre-progression");
                barre.setPrefWidth(160);
            }

            @Override
            protected void updateItem(Double item, boolean vide) {
                super.updateItem(item, vide);
                if (vide) {
                    setGraphic(null);
                    return;
                }
                Objectif objectif = getTableView().getItems().get(getIndex());
                double valeur = objectif.getProgressionPourcentage() != null
                        ? objectif.getProgressionPourcentage().doubleValue() / 100.0 : 0.0;
                barre.setProgress(Math.min(1.0, Math.max(0.0, valeur)));
                setGraphic(barre);
            }
        });
    }

    @FXML
    private void rechercher() {
        Map<String, String> filtres = new HashMap<>();
        if (comboPortee.getValue() != null) {
            filtres.put("portee", comboPortee.getValue().toString());
        }
        new Thread(() -> {
            try {
                var page = objectifsApi.rechercher(filtres);
                Platform.runLater(() -> tableObjectifs.getItems().setAll(page.getResults()));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les objectifs."));
            }
        }).start();
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<ObjectifFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/objectif-form.fxml",
                "Nouvel objectif",
                controleur -> controleur.definirOnSucces(this::rechercher));
    }

    private void ouvrirModification(Objectif objectif) {
        DialogUtil.<ObjectifFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/objectif-form.fxml",
                "Modifier l'objectif",
                controleur -> {
                    controleur.definirObjectifExistant(objectif);
                    controleur.definirOnSucces(this::rechercher);
                });
    }

    private void supprimer(Objectif objectif) {
        boolean confirme = AlertUtil.confirmation("Confirmation", "Supprimer l'objectif \"" + objectif.getTitre() + "\" ?");
        if (!confirme) {
            return;
        }
        new Thread(() -> {
            try {
                objectifsApi.supprimer(objectif.getId());
                Platform.runLater(this::rechercher);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de joindre le serveur."));
            }
        }).start();
    }
}