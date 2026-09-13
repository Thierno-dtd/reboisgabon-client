package com.reboisgabon.client.controllers;

import com.reboisgabon.client.session.Role;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.ContentHost;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ShellController implements Initializable {

    @FXML
    private StackPane zoneContenu;

    @FXML
    private Label libelleTitreEcran;

    @FXML
    private Label libelleNomUtilisateur;

    @FXML
    private Label libelleRoleUtilisateur;

    @FXML
    private Button boutonSites;

    @FXML
    private Button boutonCampagnes;

    @FXML
    private Button boutonSuivis;

    @FXML
    private Button boutonEssences;

    @FXML
    private Button boutonObjectifs;

    @FXML
    private Button boutonFinances;

    @FXML
    private Button boutonIntelligence;

    @FXML
    private Button boutonUtilisateurs;

    @FXML
    private Button boutonJournal;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ContentHost.getInstance().definirConteneur(zoneContenu);
        var utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        if (utilisateur != null) {
            libelleNomUtilisateur.setText(utilisateur.getNomComplet());
            libelleRoleUtilisateur.setText(utilisateur.getRole().toString());
        }
        appliquerVisibilitePermissions();
        allerDashboard();
    }

    private void appliquerVisibilitePermissions() {
        SessionManager session = SessionManager.getInstance();
        boolean estAdmin = session.getRole() == Role.ADMIN;
        definirVisibilite(boutonSites, session.peutAcceder("sites", "view"));
        definirVisibilite(boutonCampagnes, session.peutAcceder("campagnes", "view"));
        definirVisibilite(boutonSuivis, session.peutAcceder("suivis", "view"));
        definirVisibilite(boutonEssences, session.peutAcceder("essences", "view"));
        definirVisibilite(boutonObjectifs, session.peutAcceder("objectifs", "view"));
        definirVisibilite(boutonFinances, session.peutAcceder("finances", "view"));
        definirVisibilite(boutonIntelligence, session.peutAcceder("intelligence", "view"));
        definirVisibilite(boutonUtilisateurs, estAdmin);
        definirVisibilite(boutonJournal, estAdmin);
    }

    private void definirVisibilite(Button bouton, boolean visible) {
        bouton.setVisible(visible);
        bouton.setManaged(visible);
    }

    @FXML
    private void allerDashboard() {
        libelleTitreEcran.setText("Tableau de bord");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/dashboard.fxml");
    }

    @FXML
    private void allerSites() {
        libelleTitreEcran.setText("Sites de reboisement");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/sites.fxml");
    }

    @FXML
    private void allerCampagnes() {
        libelleTitreEcran.setText("Campagnes de plantation");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/campagnes.fxml");
    }

    @FXML
    private void allerSuivis() {
        libelleTitreEcran.setText("Suivis de croissance");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/suivis.fxml");
    }

    @FXML
    private void allerEssences() {
        libelleTitreEcran.setText("Essences");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/essences.fxml");
    }

    @FXML
    private void allerObjectifs() {
        libelleTitreEcran.setText("Objectifs de reboisement");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/objectifs.fxml");
    }

    @FXML
    private void allerFinances() {
        libelleTitreEcran.setText("Finances");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/bienvenue.fxml");
    }

    @FXML
    private void allerIntelligence() {
        libelleTitreEcran.setText("Intelligence écologique");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/bienvenue.fxml");
    }

    @FXML
    private void allerUtilisateurs() {
        libelleTitreEcran.setText("Utilisateurs");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/bienvenue.fxml");
    }

    @FXML
    private void allerJournal() {
        libelleTitreEcran.setText("Journal d'activité");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/bienvenue.fxml");
    }

    @FXML
    private void allerNotifications() {
        libelleTitreEcran.setText("Notifications");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/bienvenue.fxml");
    }

    @FXML
    private void allerParametres() {
        libelleTitreEcran.setText("Paramètres du compte");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/bienvenue.fxml");
    }

    @FXML
    private void allerExports() {
        libelleTitreEcran.setText("Exports");
        ContentHost.getInstance().afficher("/com/reboisgabon/client/fxml/bienvenue.fxml");
    }

    @FXML
    private void seDeconnecter() {
        SessionManager.getInstance().vider();
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/login.fxml");
    }
}