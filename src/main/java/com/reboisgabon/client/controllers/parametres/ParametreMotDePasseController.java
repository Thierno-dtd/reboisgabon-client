package com.reboisgabon.client.controllers.parametres;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.MeApi;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ParametreMotDePasseController {

    @FXML private PasswordField champAncien;
    @FXML private PasswordField champNouveau;
    @FXML private Label libelleMessage;
    @FXML private Button boutonValider;

    private final MeApi meApi = new MeApi();

    @FXML
    private void changerMotDePasse() {
        if (champAncien.getText() == null || champAncien.getText().isBlank()
                || champNouveau.getText() == null || champNouveau.getText().isBlank()) {
            afficherMessage("Veuillez remplir les deux champs.", "badge-erreur");
            return;
        }
        masquerMessage();
        boutonValider.setDisable(true);
        new Thread(() -> {
            try {
                meApi.changerMotDePasse(champAncien.getText(), champNouveau.getText());
                Platform.runLater(() -> {
                    boutonValider.setDisable(false);
                    champAncien.clear();
                    champNouveau.clear();
                    afficherMessage("Mot de passe modifié.", "badge-succes");
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonValider.setDisable(false);
                    afficherMessage(ErreurApiUtil.message(e), "badge-erreur");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonValider.setDisable(false);
                    afficherMessage("Impossible de joindre le serveur.", "badge-erreur");
                });
            }
        }).start();
    }

    private void afficherMessage(String message, String classeStyle) {
        libelleMessage.getStyleClass().removeAll("badge-erreur", "badge-succes");
        libelleMessage.getStyleClass().add(classeStyle);
        libelleMessage.setText(message);
        libelleMessage.setVisible(true);
        libelleMessage.setManaged(true);
    }

    private void masquerMessage() {
        libelleMessage.setVisible(false);
        libelleMessage.setManaged(false);
    }
}