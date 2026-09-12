package com.reboisgabon.client.controllers;

import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class EspaceTemporaireController implements Initializable {

    @FXML
    private Label libelleUtilisateur;

    @FXML
    private Label libelleRole;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        if (utilisateur != null) {
            libelleUtilisateur.setText(utilisateur.getNomComplet());
            libelleRole.setText("Rôle : " + utilisateur.getRole());
        }
    }

    @FXML
    private void seDeconnecter() {
        SessionManager.getInstance().vider();
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/login.fxml");
    }
}