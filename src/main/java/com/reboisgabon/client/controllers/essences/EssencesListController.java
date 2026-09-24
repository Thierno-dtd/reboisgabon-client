package com.reboisgabon.client.controllers.essences;

import com.reboisgabon.client.ui.Cellules;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.EssencesApi;
import com.reboisgabon.client.dto.essences.Essence;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.ErreurApiUtil;
import com.reboisgabon.client.util.EssenceCache;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import com.reboisgabon.client.util.BoutonIconeUtil;
import com.reboisgabon.client.util.FiltreAutoUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class EssencesListController implements Initializable {

    @FXML private TextField champRecherche;
    @FXML private Button boutonNouvelleEssence;
    @FXML private TableView<Essence> tableEssences;
    @FXML private TableColumn<Essence, String> colonneNom;
    @FXML private TableColumn<Essence, String> colonneNomScientifique;
    @FXML private TableColumn<Essence, String> colonneDescription;
    @FXML private TableColumn<Essence, Void> colonneActions;

    private final EssencesApi essencesApi = new EssencesApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colonneNom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getNom()));

        colonneNomScientifique.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getNomScientifique()));

        colonneDescription.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getDescription()));

        construireColonneActions();
        Cellules.rendu(colonneNom, v -> {
            javafx.scene.layout.StackPane vignette = new javafx.scene.layout.StackPane(com.reboisgabon.client.ui.Illustrations.feuille(
                    com.reboisgabon.client.ui.Illustrations.especePour(v.toString()), 30, javafx.scene.paint.Color.web("#1F5136"), javafx.scene.paint.Color.web("#E4EFDE")));
            vignette.setMinSize(34, 34);
            vignette.setMaxSize(34, 34);
            vignette.setStyle("-fx-background-color: #F4F7F1; -fx-background-radius: 6;");
            javafx.scene.layout.HBox bloc = new javafx.scene.layout.HBox(10, vignette, Cellules.principal(v));
            bloc.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            return bloc;
        });
        Cellules.rendu(colonneNomScientifique, v -> {
            javafx.scene.control.Label l = new javafx.scene.control.Label(v.toString());
            l.getStyleClass().add("nom-scientifique");
            return l;
        });
        Cellules.preparer(tableEssences, "Aucune essence", "Ajoutez les espèces plantées dans le programme.");

        boolean peutCreer = SessionManager.getInstance().peutAcceder("essences", "create");
        boutonNouvelleEssence.setVisible(peutCreer);
        boutonNouvelleEssence.setManaged(peutCreer);

        FiltreAutoUtil.surSaisie(champRecherche, this::rechercher);

        rechercher();
    }

    private void construireColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {

            private final Button boutonModifier = BoutonIconeUtil.creer("✏️", "Modifier");
            private final Button boutonSupprimer = BoutonIconeUtil.creer("🗑️", "Supprimer", "bouton-icone-danger");
            private final HBox conteneur = new HBox(4, boutonModifier, boutonSupprimer);

            {
                conteneur.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }

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
                boolean peutModifier = SessionManager.getInstance().peutAcceder("essences", "edit");
                boolean peutSupprimer = SessionManager.getInstance().peutAcceder("essences", "delete");
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
        new Thread(() -> {
            try {
                var page = essencesApi.rechercher(champRecherche.getText() == null ? "" : champRecherche.getText());
                Platform.runLater(() -> tableEssences.getItems().setAll(page.getResults()));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les essences."));
            }
        }).start();
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<EssenceFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/essence-form.fxml",
                "Nouvelle essence",
                controleur -> controleur.definirOnSucces(this::rafraichirApresModification));
    }

    private void ouvrirModification(Essence essence) {
        DialogUtil.<EssenceFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/essence-form.fxml",
                "Modifier l'essence",
                controleur -> {
                    controleur.definirEssenceExistante(essence);
                    controleur.definirOnSucces(this::rafraichirApresModification);
                });
    }

    private void rafraichirApresModification() {
        EssenceCache.getInstance().invalider();
        rechercher();
    }

    private void supprimer(Essence essence) {
        boolean confirme = AlertUtil.confirmation("Confirmation", "Supprimer l'essence \"" + essence.getNom() + "\" ?");
        if (!confirme) {
            return;
        }
        new Thread(() -> {
            try {
                essencesApi.supprimer(essence.getId());
                Platform.runLater(this::rafraichirApresModification);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de joindre le serveur."));
            }
        }).start();
    }
}