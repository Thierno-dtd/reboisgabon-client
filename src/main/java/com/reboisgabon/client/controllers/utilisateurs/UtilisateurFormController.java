package com.reboisgabon.client.controllers.utilisateurs;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.UsersApi;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurCreationRequete;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurModificationRequete;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurSimple;
import com.reboisgabon.client.session.Role;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class UtilisateurFormController implements Initializable {

    @FXML private Label libelleTitre;
    @FXML private TextField champEmail;
    @FXML private TextField champPrenom;
    @FXML private TextField champNom;
    @FXML private ComboBox<Role> comboRole;
    @FXML private VBox conteneurMotDePasse;
    @FXML private PasswordField champMotDePasse;
    @FXML private CheckBox caseActif;
    @FXML private Label libelleErreur;
    @FXML private Button boutonEnregistrer;

    private final UsersApi usersApi = new UsersApi();
    private UtilisateurSimple utilisateurExistant;
    private Runnable onSucces;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboRole.getItems().setAll(Role.values());
    }

    public void definirUtilisateurExistant(UtilisateurSimple utilisateur) {
        this.utilisateurExistant = utilisateur;
        libelleTitre.setText("Modifier l'utilisateur");
        champEmail.setText(utilisateur.getEmail());
        champEmail.setDisable(true);
        champPrenom.setText(utilisateur.getFirstName());
        champNom.setText(utilisateur.getLastName());
        comboRole.setValue(utilisateur.getRole());
        caseActif.setSelected(utilisateur.isActif());
        conteneurMotDePasse.setVisible(false);
        conteneurMotDePasse.setManaged(false);
    }

    public void definirOnSucces(Runnable onSucces) {
        this.onSucces = onSucces;
    }

    @FXML
    private void enregistrer() {
        if (champPrenom.getText() == null || champPrenom.getText().isBlank()
                || champNom.getText() == null || champNom.getText().isBlank()
                || comboRole.getValue() == null) {
            afficherErreur("Prénom, nom et rôle sont obligatoires.");
            return;
        }
        masquerErreur();
        boutonEnregistrer.setDisable(true);
        if (utilisateurExistant == null) {
            creerUtilisateur();
        } else {
            modifierUtilisateur();
        }
    }

    private void creerUtilisateur() {
        if (champEmail.getText() == null || champEmail.getText().isBlank()) {
            boutonEnregistrer.setDisable(false);
            afficherErreur("L'email est obligatoire.");
            return;
        }
        UtilisateurCreationRequete requete = new UtilisateurCreationRequete();
        requete.setEmail(champEmail.getText());
        requete.setFirstName(champPrenom.getText());
        requete.setLastName(champNom.getText());
        requete.setRole(comboRole.getValue());
        if (champMotDePasse.getText() != null && !champMotDePasse.getText().isBlank()) {
            requete.setPassword(champMotDePasse.getText());
        }
        new Thread(() -> {
            try {
                usersApi.creer(requete);
                Platform.runLater(this::terminerAvecSucces);
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

    private void modifierUtilisateur() {
        UtilisateurModificationRequete requete = new UtilisateurModificationRequete();
        requete.setFirstName(champPrenom.getText());
        requete.setLastName(champNom.getText());
        requete.setRole(comboRole.getValue());
        requete.setActif(caseActif.isSelected());
        new Thread(() -> {
            try {
                usersApi.modifier(utilisateurExistant.getId(), requete);
                Platform.runLater(this::terminerAvecSucces);
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

    private void terminerAvecSucces() {
        if (onSucces != null) {
            onSucces.run();
        }
        fermer();
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        ((Stage) champEmail.getScene().getWindow()).close();
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