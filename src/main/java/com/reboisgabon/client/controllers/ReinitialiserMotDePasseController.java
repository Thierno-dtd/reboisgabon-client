package com.reboisgabon.client.controllers;

import com.reboisgabon.client.api.endpoints.AuthApi;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ReinitialiserMotDePasseController {

    @FXML
    private TextField champToken;

    @FXML
    private PasswordField champNouveauMotDePasse;

    @FXML
    private Label libelleMessage;

    @FXML
    private Button boutonValider;

    private final AuthApi authApi = new AuthApi();

    @FXML
    private void reinitialiser() {
        String token = champToken.getText();
        String nouveauMotDePasse = champNouveauMotDePasse.getText();
        if (token == null || token.isBlank() || nouveauMotDePasse == null || nouveauMotDePasse.isBlank()) {
            afficherMessage("Renseignez le code reçu et votre nouveau mot de passe.", "message-erreur");
            return;
        }
        boutonValider.setDisable(true);
        new Thread(() -> {
            try {
                authApi.reinitialiserMotDePasse(token, nouveauMotDePasse);
                Platform.runLater(() -> {
                    com.reboisgabon.client.util.AlertUtil.information("Mot de passe mis à jour", "Votre nouveau mot de passe est enregistré. Connectez-vous avec celui-ci.");
                    SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/login.fxml");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonValider.setDisable(false);
                    afficherMessage("Ce code est invalide, expiré ou déjà utilisé. Demandez-en un nouveau.", "message-erreur");
                });
            }
        }).start();
    }

    @FXML
    private void retourLogin() {
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/login.fxml");
    }

    private void afficherMessage(String message, String classeStyle) {
        libelleMessage.getStyleClass().removeAll("message-erreur", "message-succes");
        libelleMessage.getStyleClass().add(classeStyle);
        libelleMessage.setText(message);
        libelleMessage.setVisible(true);
        libelleMessage.setManaged(true);
    }
}