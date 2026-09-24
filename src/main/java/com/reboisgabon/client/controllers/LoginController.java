package com.reboisgabon.client.controllers;

import com.reboisgabon.client.api.endpoints.AuthApi;
import com.reboisgabon.client.dto.auth.ConnexionReponse;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.ui.Icones;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        Platform.runLater(() -> emailField.requestFocus());
    }

    @FXML
    private void handleTogglePassword() {
        isPasswordVisible = !isPasswordVisible;
        passwordTextField.setVisible(isPasswordVisible);
        passwordTextField.setManaged(isPasswordVisible);
        passwordField.setVisible(!isPasswordVisible);
        passwordField.setManaged(!isPasswordVisible);
        togglePasswordBtn.setGraphic(Icones.icone(isPasswordVisible ? Icones.OEIL_BARRE : Icones.OEIL));
    }

    @FXML
    private void motDePasseOublie() {
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/mot-de-passe-oublie.fxml");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isBlank() || password == null || password.isBlank()) {
            showError("Saisissez votre adresse e-mail et votre mot de passe.");
            return;
        }
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        definirChargement(true);

        new Thread(() -> {
            try {
                AuthApi authApi = new AuthApi();
                ConnexionReponse resp = authApi.connexion(email, password);
                if (resp.isRequiresTwoFa()) {
                    Platform.runLater(() -> SceneNavigator.getInstance().naviguerVers(
                            "/com/reboisgabon/client/fxml/verification-2fa.fxml",
                            (Verification2faController controller) -> controller.definirTempToken(resp.getTempToken())));
                    return;
                }
                SessionManager.getInstance().setAccessToken(resp.getAccess());
                SessionManager.getInstance().setRefreshToken(resp.getRefresh());
                var profil = authApi.recupererProfil();
                var permissions = authApi.recupererPermissions();
                SessionManager.getInstance().setUtilisateurConnecte(profil);
                SessionManager.getInstance().setPermissions(permissions);
                Platform.runLater(() -> SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/shell.fxml"));
            } catch (Exception e) {
                SessionManager.getInstance().vider();
                Platform.runLater(() -> {
                    definirChargement(false);
                    showError("Identifiants incorrects, compte désactivé ou serveur injoignable. Vérifiez vos informations puis réessayez.");
                });
            }
        }).start();
    }

    private void definirChargement(boolean enCours) {
        loginButton.setDisable(enCours);
        loginButton.setText(enCours ? "Connexion en cours…" : "Se connecter");
        emailField.setDisable(enCours);
        passwordField.setDisable(enCours);
        passwordTextField.setDisable(enCours);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
