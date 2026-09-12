package com.reboisgabon.client.controllers;

import com.reboisgabon.client.api.endpoints.AuthApi;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MotDePasseOublieController {

    @FXML
    private TextField champEmail;

    @FXML
    private Label libelleMessage;

    @FXML
    private Button boutonEnvoyer;

    private final AuthApi authApi = new AuthApi();

    @FXML
    private void envoyerDemande() {
        String email = champEmail.getText();
        if (email == null || email.isBlank()) {
            afficherMessage("Veuillez saisir votre email.", "badge-erreur");
            return;
        }
        boutonEnvoyer.setDisable(true);
        new Thread(() -> {
            try {
                authApi.demanderReinitialisation(email);
                Platform.runLater(() -> {
                    boutonEnvoyer.setDisable(false);
                    afficherMessage("Si ce compte existe, un email a été envoyé.", "badge-succes");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonEnvoyer.setDisable(false);
                    afficherMessage("Une erreur est survenue.", "badge-erreur");
                });
            }
        }).start();
    }

    @FXML
    private void allerReinitialiser() {
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/reinitialiser-mot-de-passe.fxml");
    }

    @FXML
    private void retourLogin() {
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/login.fxml");
    }

    private void afficherMessage(String message, String classeStyle) {
        libelleMessage.getStyleClass().removeAll("badge-erreur", "badge-succes");
        libelleMessage.getStyleClass().add(classeStyle);
        libelleMessage.setText(message);
        libelleMessage.setVisible(true);
        libelleMessage.setManaged(true);
    }
}