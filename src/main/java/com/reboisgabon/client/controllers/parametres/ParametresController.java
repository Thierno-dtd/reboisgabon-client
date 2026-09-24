package com.reboisgabon.client.controllers.parametres;

import com.reboisgabon.client.config.AppConfig;
import com.reboisgabon.client.session.Role;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.Icones;
import com.reboisgabon.client.ui.Navigation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ParametresController implements Initializable {

    @FXML private Label libelleAvatar;
    @FXML private Label libelleNom;
    @FXML private Label libelleEmail;
    @FXML private HBox zonePastilles;
    @FXML private Button ongletProfil;
    @FXML private Button ongletMotDePasse;
    @FXML private Button ongletDeuxFacteurs;
    @FXML private Button ongletSession;
    @FXML private Node pageProfil;
    @FXML private Node pageMotDePasse;
    @FXML private Node pageDeuxFacteurs;
    @FXML private VBox pageSession;

    private static final String[][] DOMAINES = {
            {"sites", "Sites de reboisement"}, {"campagnes", "Campagnes"}, {"suivis", "Suivis de croissance"},
            {"essences", "Essences"}, {"objectifs", "Objectifs"}, {"finances", "Finances"},
            {"intelligence", "Intelligence écologique"}, {"utilisateurs", "Utilisateurs"}, {"audit", "Journal d'activité"}
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        if (utilisateur != null) {
            libelleAvatar.setText(Composants.initiales(utilisateur.getNomComplet()));
            libelleNom.setText(utilisateur.getNomComplet());
            libelleEmail.setText(utilisateur.getEmail());
            zonePastilles.getChildren().setAll(
                    Composants.pastille(libelleRole(utilisateur.getRole()), "or"),
                    utilisateur.isTwoFaEnabled() ? Composants.pastille("2FA activée", "foret") : Composants.pastille("2FA inactive", "neutre"));
        }
        construireSession();
        afficherProfil();
    }

    private String libelleRole(Role role) {
        if (role == null) {
            return "—";
        }
        return switch (role) {
            case ADMIN -> "Administrateur";
            case SUPERVISEUR -> "Superviseur";
            case AGENT -> "Agent de terrain";
            case FINANCIER -> "Financier";
        };
    }

    private void montrer(Node page, Button onglet) {
        for (Node p : List.of(pageProfil, pageMotDePasse, pageDeuxFacteurs, pageSession)) {
            p.setVisible(p == page);
            p.setManaged(p == page);
        }
        for (Button b : List.of(ongletProfil, ongletMotDePasse, ongletDeuxFacteurs, ongletSession)) {
            b.getStyleClass().remove("active");
        }
        onglet.getStyleClass().add("active");
    }

    @FXML
    private void afficherProfil() {
        montrer(pageProfil, ongletProfil);
    }

    @FXML
    private void afficherMotDePasse() {
        montrer(pageMotDePasse, ongletMotDePasse);
    }

    @FXML
    private void afficherDeuxFacteurs() {
        montrer(pageDeuxFacteurs, ongletDeuxFacteurs);
    }

    @FXML
    private void afficherSession() {
        montrer(pageSession, ongletSession);
    }

    private void construireSession() {
        Label titre = new Label("Session et permissions");
        titre.getStyleClass().add("titre-section");
        Label sousTitre = new Label("Les droits ci-dessous sont définis par l'administrateur selon votre rôle et vérifiés par le serveur à chaque requête.");
        sousTitre.getStyleClass().add("texte-muet");
        sousTitre.setWrapText(true);
        VBox entete = new VBox(2, titre, sousTitre);
        entete.getStyleClass().add("feuille-titre");

        GridPane grille = new GridPane();
        grille.setHgap(10);
        grille.setVgap(0);
        ColumnConstraints premiere = new ColumnConstraints();
        premiere.setHgrow(Priority.ALWAYS);
        grille.getColumnConstraints().add(premiere);
        String[] actions = {"view", "create", "edit", "delete"};
        String[] libelles = {"Consulter", "Créer", "Modifier", "Supprimer"};
        for (int i = 0; i < actions.length; i++) {
            ColumnConstraints c = new ColumnConstraints(84);
            c.setHalignment(HPos.CENTER);
            grille.getColumnConstraints().add(c);
            Label l = new Label(libelles[i].toUpperCase());
            l.getStyleClass().add("cle-etiquette");
            grille.add(l, i + 1, 0);
        }
        Label domaineTitre = new Label("DOMAINE");
        domaineTitre.getStyleClass().add("cle-etiquette");
        grille.add(domaineTitre, 0, 0);
        SessionManager session = SessionManager.getInstance();
        boolean admin = session.getRole() == Role.ADMIN;
        for (int r = 0; r < DOMAINES.length; r++) {
            Label domaine = new Label(DOMAINES[r][1]);
            domaine.getStyleClass().add("texte-corps");
            domaine.setPadding(new Insets(9, 0, 9, 0));
            grille.add(domaine, 0, r + 1);
            for (int a = 0; a < actions.length; a++) {
                boolean autorise = admin || session.peutAcceder(DOMAINES[r][0], actions[a]);
                Node marque = autorise
                        ? Icones.icone(Icones.VALIDE, 18, Color.web("#1F5136"))
                        : Icones.icone("mdi2m-minus", 16, Color.web("#B8C2B4"));
                grille.add(marque, a + 1, r + 1);
            }
        }
        grille.setPadding(new Insets(4, 0, 0, 0));

        VBox infos = new VBox(0,
                ligneInfo(Icones.COMPTE, "Connecté en tant que", session.getUtilisateurConnecte() != null ? session.getUtilisateurConnecte().getEmail() : "—"),
                ligneInfo("mdi2d-database-outline", "Serveur", AppConfig.API_BASE_URL),
                ligneInfo(Icones.INFO, "Version", "ReboisGabon 1.0.0 — client JavaFX"));

        Button deconnexion = Composants.bouton("Se déconnecter", Icones.DECONNEXION, "bouton-secondaire");
        deconnexion.setOnAction(e -> Navigation.deconnecter());
        HBox actionsBas = new HBox(deconnexion);
        actionsBas.setAlignment(Pos.CENTER_RIGHT);

        VBox corps = new VBox(18, infos, grille, actionsBas);
        corps.setPadding(new Insets(16, 22, 22, 22));
        pageSession.getChildren().setAll(entete, corps);
    }

    private HBox ligneInfo(String icone, String cle, String valeur) {
        Label c = new Label(cle);
        c.getStyleClass().add("texte-muet");
        c.setMinWidth(150);
        Label v = new Label(valeur);
        v.getStyleClass().add("texte-corps");
        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);
        HBox ligne = new HBox(10, Icones.icone(icone, 17, Color.web("#46594B")), c, v);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.getStyleClass().add("ligne-liste");
        ligne.setPadding(new Insets(9, 0, 9, 0));
        return ligne;
    }
}
