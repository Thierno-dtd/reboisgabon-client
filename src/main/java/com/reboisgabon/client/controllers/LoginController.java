package com.reboisgabon.client.controllers;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.AuthApi;
import com.reboisgabon.client.dto.auth.ConnexionReponse;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField champEmail;

    @FXML
    private PasswordField champMotDePasse;

    @FXML
    private Label libelleErreur;

    @FXML
    private Button boutonConnexion;

    private final AuthApi authApi = new AuthApi();

    @FXML
    private void seConnecter() {
        String email = champEmail.getText();
        String motDePasse = champMotDePasse.getText();
        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            afficherErreur("Veuillez renseigner votre email et votre mot de passe.");
            return;
        }
        masquerErreur();
        boutonConnexion.setDisable(true);
        new Thread(() -> {
            try {
                ConnexionReponse reponse = authApi.connexion(email, motDePasse);
                Platform.runLater(() -> traiterReponseConnexion(reponse));
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonConnexion.setDisable(false);
                    if (e.getStatutHttp() == 429) {
                        afficherErreur("Trop de tentatives. Veuillez patienter avant de réessayer.");
                    } else {
                        afficherErreur("Email ou mot de passe incorrect.");
                    }
                });
            } catch (Exception e) {
                //e.printStackTrace();
                Platform.runLater(() -> {
                    boutonConnexion.setDisable(false);
                    afficherErreur("Impossible de joindre le serveur.");
                });
            }
        }).start();
    }

    private void traiterReponseConnexion(ConnexionReponse reponse) {
        boutonConnexion.setDisable(false);
        if (reponse.isRequiresTwoFa()) {
            SceneNavigator.getInstance().naviguerVers(
                    "/com/reboisgabon/client/fxml/verification-2fa.fxml",
                    (Verification2faController controleur) -> controleur.definirTempToken(reponse.getTempToken()));
        } else {
            SessionManager.getInstance().setAccessToken(reponse.getAccess());
            SessionManager.getInstance().setRefreshToken(reponse.getRefresh());
            chargerSessionEtBasculer();
        }
    }

    private void chargerSessionEtBasculer() {
        new Thread(() -> {
            try {
                var profil = authApi.recupererProfil();
                System.out.println("Profil récupéré : " + profil);
                var permissions = authApi.recupererPermissions();
                System.out.println("Permissions récupérées : " + permissions);
                SessionManager.getInstance().setUtilisateurConnecte(profil);
                SessionManager.getInstance().setPermissions(permissions);
                Platform.runLater(() ->
                        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/espace-temporaire.fxml"));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> afficherErreur("Connexion réussie mais profil injoignable."));
            }
        }).start();
    }

    @FXML
    private void ouvrirMotDePasseOublie() {
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/mot-de-passe-oublie.fxml");
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