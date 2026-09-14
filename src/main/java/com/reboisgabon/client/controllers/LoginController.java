package com.reboisgabon.client.controllers;

import com.reboisgabon.client.api.endpoints.AuthApi;
import com.reboisgabon.client.dto.auth.ConnexionReponse;
import com.reboisgabon.client.dto.auth.ConnexionRequete;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;

public class LoginController {

    @FXML private StackPane rootStackPane;
    @FXML private MediaView mediaView;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private Label errorLabel;

    private MediaPlayer mediaPlayer;
    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        initVideoBackground();
        setupPasswordSync();
    }

    private void initVideoBackground() {
        try {
            URL videoUrl = getClass().getResource("/com/reboisgabon/client/media/background.mp4");
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setMute(true);
                mediaPlayer.play();

                // Assurer que la vidéo s'étende sur tout l'écran de manière responsive
                mediaView.fitWidthProperty().bind(rootStackPane.widthProperty());
                mediaView.fitHeightProperty().bind(rootStackPane.heightProperty());
            }
        } catch (Exception e) {
            // Fallback si la vidéo n'est pas présente
        }
    }

    private void setupPasswordSync() {
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handleTogglePassword() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("🙈");
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordBtn.setText("👁");
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        new Thread(() -> {
            try {
                AuthApi authApi = new AuthApi();
                ConnexionReponse resp = authApi.connexion(email, password);

                Platform.runLater(() -> {
                    if (resp.isRequiresTwoFa()) {
                        SceneNavigator.getInstance().naviguerVers(
                                "/com/reboisgabon/client/fxml/verification-2fa.fxml",
                                (Verification2faController controller) -> controller.definirTempToken(resp.getTempToken())
                        );
                    } else {
                        SessionManager.getInstance().setAccessToken(resp.getAccess());
                        SessionManager.getInstance().setRefreshToken(resp.getRefresh());

                        new Thread(() -> {
                            try {
                                var profil = authApi.recupererProfil();
                                var permissions = authApi.recupererPermissions();
                                SessionManager.getInstance().setUtilisateurConnecte(profil);
                                SessionManager.getInstance().setPermissions(permissions);

                                Platform.runLater(() ->
                                        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/shell.fxml")
                                );
                            } catch (Exception e) {
                                Platform.runLater(() -> showError("Erreur lors de la récupération du profil."));
                            }
                        }).start();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Identifiants incorrects ou erreur réseau."));
            }
        }).start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}