package com.reboisgabon.client.controllers.parametres;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.AuthApi;
import com.reboisgabon.client.api.endpoints.MeApi;
import com.reboisgabon.client.dto.compte.ProfilModificationRequete;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ParametreProfilController implements Initializable {

    @FXML private TextField champPrenom;
    @FXML private TextField champNom;
    @FXML private Label libelleMessage;
    @FXML private Button boutonEnregistrer;

    private final MeApi meApi = new MeApi();
    private final AuthApi authApi = new AuthApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        if (utilisateur != null) {
            champPrenom.setText(utilisateur.getFirstName());
            champNom.setText(utilisateur.getLastName());
        }
    }

    @FXML
    private void enregistrer() {
        ProfilModificationRequete requete = new ProfilModificationRequete();
        requete.setFirstName(champPrenom.getText());
        requete.setLastName(champNom.getText());
        masquerMessage();
        boutonEnregistrer.setDisable(true);
        new Thread(() -> {
            try {
                meApi.modifierProfil(requete);
                var profilMisAJour = authApi.recupererProfil();
                SessionManager.getInstance().setUtilisateurConnecte(profilMisAJour);
                Platform.runLater(() -> {
                    boutonEnregistrer.setDisable(false);
                    afficherMessage("Profil mis à jour.", "badge-succes");
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonEnregistrer.setDisable(false);
                    afficherMessage(ErreurApiUtil.message(e), "badge-erreur");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonEnregistrer.setDisable(false);
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