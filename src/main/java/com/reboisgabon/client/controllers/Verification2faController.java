package com.reboisgabon.client.controllers;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.AuthApi;
import com.reboisgabon.client.dto.auth.TokensReponse;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Verification2faController {

    @FXML
    private TextField champCode;

    @FXML
    private Label libelleErreur;

    @FXML
    private Button boutonValider;

    private final AuthApi authApi = new AuthApi();
    private String tempToken;

    public void definirTempToken(String tempToken) {
        this.tempToken = tempToken;
    }

    @FXML
    private void validerCode() {
        String code = champCode.getText();
        if (code == null || code.isBlank()) {
            afficherErreur("Veuillez saisir le code à 6 chiffres.");
            return;
        }
        masquerErreur();
        boutonValider.setDisable(true);
        new Thread(() -> {
            try {
                TokensReponse tokens = authApi.verifierOtp(tempToken, code);
                SessionManager.getInstance().setAccessToken(tokens.getAccess());
                SessionManager.getInstance().setRefreshToken(tokens.getRefresh());
                var profil = authApi.recupererProfil();
                var permissions = authApi.recupererPermissions();
                SessionManager.getInstance().setUtilisateurConnecte(profil);
                SessionManager.getInstance().setPermissions(permissions);
                Platform.runLater(() ->
                        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/espace-temporaire.fxml"));
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonValider.setDisable(false);
                    afficherErreur("Code invalide ou expiré.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonValider.setDisable(false);
                    afficherErreur("Impossible de joindre le serveur.");
                });
            }
        }).start();
    }

    @FXML
    private void retourLogin() {
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/login.fxml");
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