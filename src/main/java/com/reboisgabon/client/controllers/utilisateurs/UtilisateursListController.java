package com.reboisgabon.client.controllers.utilisateurs;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.UsersApi;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurSimple;
import com.reboisgabon.client.session.Role;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import com.reboisgabon.client.util.BoutonIconeUtil;
import com.reboisgabon.client.util.FiltreAutoUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class UtilisateursListController implements Initializable {

    @FXML private TextField champRecherche;
    @FXML private ComboBox<Role> comboRole;
    @FXML private ComboBox<String> comboActif;
    @FXML private Button boutonNouvelUtilisateur;
    @FXML private TableView<UtilisateurSimple> tableUtilisateurs;
    @FXML private TableColumn<UtilisateurSimple, String> colonneEmail;
    @FXML private TableColumn<UtilisateurSimple, String> colonnePrenom;
    @FXML private TableColumn<UtilisateurSimple, String> colonneNom;
    @FXML private TableColumn<UtilisateurSimple, String> colonneRole;
    @FXML private TableColumn<UtilisateurSimple, Boolean> colonneActif;
    @FXML private TableColumn<UtilisateurSimple, Void> colonneActions;

    private final UsersApi usersApi = new UsersApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboRole.getItems().add(null);
        comboRole.getItems().addAll(Role.values());

        comboActif.getItems().addAll(null, "true", "false");

        colonneEmail.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getEmail() != null
                                ? data.getValue().getEmail()
                                : ""
                ));

        colonnePrenom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getFirstName() != null
                                ? data.getValue().getFirstName()
                                : ""
                ));

        colonneNom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getLastName() != null
                                ? data.getValue().getLastName()
                                : ""
                ));

        colonneRole.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getRole() != null
                                ? data.getValue().getRole().toString()
                                : ""
                ));

        colonneActif.setCellValueFactory(data ->
                new javafx.beans.property.SimpleBooleanProperty(
                        data.getValue().isActif()
                ).asObject());

        construireColonneActions();

        FiltreAutoUtil.surSaisie(champRecherche, this::rechercher);
        FiltreAutoUtil.surValeur(comboRole, this::rechercher);
        FiltreAutoUtil.surValeur(comboActif, this::rechercher);

        rechercher();
    }

    private void construireColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {

            private final Button boutonModifier = BoutonIconeUtil.creer("✏️", "Modifier");
            private final Button boutonDesactiver = BoutonIconeUtil.creer("🚫", "Désactiver", "bouton-icone-danger");
            private final Button boutonReactiver = BoutonIconeUtil.creer("✅", "Réactiver", "bouton-icone-succes");
            private final HBox conteneur = new HBox(6, boutonModifier, boutonDesactiver, boutonReactiver);

            {
                boutonModifier.setOnAction(evenement -> ouvrirModification(getTableView().getItems().get(getIndex())));
                boutonDesactiver.setOnAction(evenement -> desactiver(getTableView().getItems().get(getIndex())));
                boutonReactiver.setOnAction(evenement -> reactiver(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                if (vide) {
                    setGraphic(null);
                    return;
                }
                UtilisateurSimple utilisateur = getTableView().getItems().get(getIndex());
                boutonDesactiver.setVisible(utilisateur.isActif());
                boutonDesactiver.setManaged(utilisateur.isActif());
                boutonReactiver.setVisible(!utilisateur.isActif());
                boutonReactiver.setManaged(!utilisateur.isActif());
                setGraphic(conteneur);
            }
        });
    }

    @FXML
    private void rechercher() {
        Map<String, String> filtres = new HashMap<>();
        filtres.put("search", champRecherche.getText());
        if (comboRole.getValue() != null) {
            filtres.put("role", comboRole.getValue().toString());
        }
        if (comboActif.getValue() != null) {
            filtres.put("is_active", comboActif.getValue());
        }
        new Thread(() -> {
            try {
                var page = usersApi.lister(filtres);
                Platform.runLater(() -> tableUtilisateurs.getItems().setAll(page.getResults()));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les utilisateurs."));
            }
        }).start();
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<UtilisateurFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/utilisateur-form.fxml",
                "Nouvel utilisateur",
                controleur -> controleur.definirOnSucces(this::rechercher));
    }

    private void ouvrirModification(UtilisateurSimple utilisateur) {
        DialogUtil.<UtilisateurFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/utilisateur-form.fxml",
                "Modifier l'utilisateur",
                controleur -> {
                    controleur.definirUtilisateurExistant(utilisateur);
                    controleur.definirOnSucces(this::rechercher);
                });
    }

    private void desactiver(UtilisateurSimple utilisateur) {
        boolean confirme = AlertUtil.confirmation("Confirmation", "Désactiver le compte \"" + utilisateur.getEmail() + "\" ?");
        if (!confirme) {
            return;
        }
        new Thread(() -> {
            try {
                usersApi.supprimer(utilisateur.getId());
                Platform.runLater(this::rechercher);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de joindre le serveur."));
            }
        }).start();
    }

    private void reactiver(UtilisateurSimple utilisateur) {
        new Thread(() -> {
            try {
                usersApi.reactiver(utilisateur.getId());
                Platform.runLater(this::rechercher);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de joindre le serveur."));
            }
        }).start();
    }
}