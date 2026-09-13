package com.reboisgabon.client.controllers.parametres;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.AuthApi;
import com.reboisgabon.client.api.endpoints.MeApi;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.ErreurApiUtil;
import com.reboisgabon.client.util.QrCodeUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class Parametre2faController implements Initializable {

    @FXML private Label libelleStatut;
    @FXML private Button boutonDemarrerActivation;
    @FXML private ImageView imageQrCode;
    @FXML private Label libelleSecret;
    @FXML private TextField champCode;
    @FXML private Button boutonConfirmer;
    @FXML private Button boutonDesactiver;
    @FXML private Label libelleErreur;

    private final MeApi meApi = new MeApi();
    private final AuthApi authApi = new AuthApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rafraichirEtat();
    }

    private void rafraichirEtat() {
        var utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        boolean active = utilisateur != null && utilisateur.isTwoFaEnabled();
        libelleStatut.setText(active ? "La 2FA est actuellement activée." : "La 2FA est actuellement désactivée.");
        boutonDemarrerActivation.setVisible(!active);
        boutonDemarrerActivation.setManaged(!active);
        boutonDesactiver.setVisible(active);
        boutonDesactiver.setManaged(active);
        if (active) {
            masquerFormulaireActivation();
        }
    }

    @FXML
    private void demarrerActivation() {
        masquerErreur();
        boutonDemarrerActivation.setDisable(true);
        new Thread(() -> {
            try {
                var reponse = meApi.initialiser2fa();
                var qrCode = QrCodeUtil.genererQrCode(reponse.getProvisioningUri(), 220);
                Platform.runLater(() -> {
                    boutonDemarrerActivation.setDisable(false);
                    imageQrCode.setImage(qrCode);
                    imageQrCode.setVisible(true);
                    imageQrCode.setManaged(true);
                    libelleSecret.setText("Code secret manuel : " + reponse.getSecret());
                    libelleSecret.setVisible(true);
                    libelleSecret.setManaged(true);
                    champCode.setVisible(true);
                    champCode.setManaged(true);
                    boutonConfirmer.setVisible(true);
                    boutonConfirmer.setManaged(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonDemarrerActivation.setDisable(false);
                    afficherErreur("Impossible de démarrer l'activation de la 2FA.");
                });
            }
        }).start();
    }

    @FXML
    private void confirmerActivation() {
        if (champCode.getText() == null || champCode.getText().isBlank()) {
            afficherErreur("Veuillez saisir le code affiché par votre application.");
            return;
        }
        masquerErreur();
        boutonConfirmer.setDisable(true);
        new Thread(() -> {
            try {
                meApi.confirmer2fa(champCode.getText());
                var profil = authApi.recupererProfil();
                SessionManager.getInstance().setUtilisateurConnecte(profil);
                Platform.runLater(() -> {
                    boutonConfirmer.setDisable(false);
                    masquerFormulaireActivation();
                    rafraichirEtat();
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonConfirmer.setDisable(false);
                    afficherErreur(ErreurApiUtil.message(e));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonConfirmer.setDisable(false);
                    afficherErreur("Impossible de joindre le serveur.");
                });
            }
        }).start();
    }

    @FXML
    private void desactiver() {
        boutonDesactiver.setDisable(true);
        new Thread(() -> {
            try {
                meApi.desactiver2fa();
                var profil = authApi.recupererProfil();
                SessionManager.getInstance().setUtilisateurConnecte(profil);
                Platform.runLater(() -> {
                    boutonDesactiver.setDisable(false);
                    rafraichirEtat();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonDesactiver.setDisable(false);
                    afficherErreur("Impossible de désactiver la 2FA.");
                });
            }
        }).start();
    }

    private void masquerFormulaireActivation() {
        imageQrCode.setVisible(false);
        imageQrCode.setManaged(false);
        libelleSecret.setVisible(false);
        libelleSecret.setManaged(false);
        champCode.clear();
        champCode.setVisible(false);
        champCode.setManaged(false);
        boutonConfirmer.setVisible(false);
        boutonConfirmer.setManaged(false);
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