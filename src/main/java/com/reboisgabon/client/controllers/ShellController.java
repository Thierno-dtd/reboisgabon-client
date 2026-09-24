package com.reboisgabon.client.controllers;

import com.reboisgabon.client.api.endpoints.NotificationsApi;
import com.reboisgabon.client.session.Role;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.Illustrations;
import com.reboisgabon.client.ui.Navigation;
import com.reboisgabon.client.util.ContentHost;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ShellController implements Initializable {

    @FXML private StackPane zoneContenu;
    @FXML private ScrollPane defilementContenu;
    @FXML private HBox zoneMarque;
    @FXML private VBox decorBarreLaterale;
    @FXML private Label libelleDate;
    @FXML private Label libelleNomUtilisateur;
    @FXML private Label libelleRoleUtilisateur;
    @FXML private Label libelleInitiales;
    @FXML private Label badgeNotifications;
    @FXML private Label groupeTerrain;
    @FXML private Label groupeProgramme;
    @FXML private Label groupeAdministration;
    @FXML private Button boutonDashboard;
    @FXML private Button boutonCarte;
    @FXML private Button boutonSites;
    @FXML private Button boutonCampagnes;
    @FXML private Button boutonSuivis;
    @FXML private Button boutonEssences;
    @FXML private Button boutonObjectifs;
    @FXML private Button boutonFinances;
    @FXML private Button boutonIntelligence;
    @FXML private Button boutonUtilisateurs;
    @FXML private Button boutonJournal;
    @FXML private Button boutonParametres;
    @FXML private Button boutonNotifications;
    @FXML private Button boutonDeconnexion;

    private final NotificationsApi notificationsApi = new NotificationsApi();
    private final Map<Navigation.Ecran, Destination> destinations = new EnumMap<>(Navigation.Ecran.class);
    private Timeline minuteurNotifications;

    private record Destination(Button bouton, String section, String titre, String fxml) {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ContentHost.getInstance().definirConteneur(zoneContenu);

        zoneMarque.getChildren().add(0, Illustrations.logo(38));
        Illustrations.Canopee canopee = new Illustrations.Canopee(Color.web("#0D1F15"), 110, 11);
        Illustrations.CourbesNiveau courbes = new Illustrations.CourbesNiveau(Color.web("#9DB8A4"), 0.10, 5);
        VBox.setVgrow(courbes, Priority.ALWAYS);
        decorBarreLaterale.getChildren().addAll(courbes, canopee);

        libelleDate.setText(capitaliser(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRANCE))));
        boutonNotifications.setTooltip(new Tooltip("Notifications"));
        boutonDeconnexion.setTooltip(new Tooltip("Se déconnecter"));

        var utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        if (utilisateur != null) {
            libelleNomUtilisateur.setText(utilisateur.getNomComplet());
            libelleInitiales.setText(Composants.initiales(utilisateur.getNomComplet()));
            if (utilisateur.getRole() != null) {
                libelleRoleUtilisateur.setText(libelleRole(utilisateur.getRole()));
            }
        }

        declarerDestinations();
        appliquerVisibilitePermissions();
        Navigation.definirHote(this::aller);
        Navigation.definirDeconnexion(this::seDeconnecter);
        aller(Navigation.Ecran.TABLEAU_DE_BORD, null);
        demarrerRafraichissementNotifications();
    }

    private void declarerDestinations() {
        String fx = "/com/reboisgabon/client/fxml/";
        destinations.put(Navigation.Ecran.TABLEAU_DE_BORD, new Destination(boutonDashboard, "Pilotage", "Tableau de bord", fx + "dashboard.fxml"));
        destinations.put(Navigation.Ecran.CARTE, new Destination(boutonCarte, "Pilotage", "Carte du territoire", fx + "carte.fxml"));
        destinations.put(Navigation.Ecran.SITES, new Destination(boutonSites, "Terrain", "Sites de reboisement", fx + "sites-liste.fxml"));
        destinations.put(Navigation.Ecran.CAMPAGNES, new Destination(boutonCampagnes, "Terrain", "Campagnes de plantation", fx + "campagnes.fxml"));
        destinations.put(Navigation.Ecran.SUIVIS, new Destination(boutonSuivis, "Terrain", "Suivis de croissance", fx + "suivis.fxml"));
        destinations.put(Navigation.Ecran.ESSENCES, new Destination(boutonEssences, "Terrain", "Essences", fx + "essences.fxml"));
        destinations.put(Navigation.Ecran.OBJECTIFS, new Destination(boutonObjectifs, "Programme", "Objectifs de reboisement", fx + "objectifs.fxml"));
        destinations.put(Navigation.Ecran.FINANCES, new Destination(boutonFinances, "Programme", "Finances", fx + "finances.fxml"));
        destinations.put(Navigation.Ecran.INTELLIGENCE, new Destination(boutonIntelligence, "Programme", "Intelligence écologique", fx + "intelligence.fxml"));
        destinations.put(Navigation.Ecran.UTILISATEURS, new Destination(boutonUtilisateurs, "Administration", "Utilisateurs", fx + "utilisateurs.fxml"));
        destinations.put(Navigation.Ecran.JOURNAL, new Destination(boutonJournal, "Administration", "Journal d'activité", fx + "journal.fxml"));
        destinations.put(Navigation.Ecran.PARAMETRES, new Destination(boutonParametres, "Compte", "Paramètres", fx + "parametres.fxml"));
        destinations.put(Navigation.Ecran.NOTIFICATIONS, new Destination(null, "Compte", "Notifications", fx + "notifications.fxml"));
    }

    private <T> void aller(Navigation.Ecran ecran, Consumer<T> configuration) {
        Destination destination = destinations.get(ecran);
        if (destination == null) {
            return;
        }
        activerNavigation(destination.bouton());
        SceneNavigator.getInstance().getStagePrincipal().setTitle("ReboisGabon — " + destination.titre());
        ContentHost.getInstance().afficher(destination.fxml(), configuration);
        defilementContenu.setVvalue(0);
        if (ecran == Navigation.Ecran.NOTIFICATIONS) {
            Platform.runLater(this::rafraichirBadgeNotifications);
        }
    }

    private String libelleRole(Role role) {
        return switch (role) {
            case ADMIN -> "Administrateur";
            case SUPERVISEUR -> "Superviseur";
            case AGENT -> "Agent de terrain";
            case FINANCIER -> "Financier";
        };
    }

    private String capitaliser(String texte) {
        return texte.isEmpty() ? texte : texte.substring(0, 1).toUpperCase(Locale.FRANCE) + texte.substring(1);
    }

    private void demarrerRafraichissementNotifications() {
        rafraichirBadgeNotifications();
        minuteurNotifications = new Timeline(new KeyFrame(Duration.seconds(60), evenement -> rafraichirBadgeNotifications()));
        minuteurNotifications.setCycleCount(Timeline.INDEFINITE);
        minuteurNotifications.play();
    }

    private void rafraichirBadgeNotifications() {
        Thread thread = new Thread(() -> {
            try {
                int nombre = notificationsApi.compterNonLues();
                Platform.runLater(() -> {
                    badgeNotifications.setText(nombre > 99 ? "99+" : String.valueOf(nombre));
                    badgeNotifications.setVisible(nombre > 0);
                });
            } catch (Exception ignored) {
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void appliquerVisibilitePermissions() {
        SessionManager session = SessionManager.getInstance();
        boolean estAdmin = session.getRole() == Role.ADMIN;

        definirVisibilite(boutonSites, session.peutAcceder("sites", "view"));
        definirVisibilite(boutonCarte, session.peutAcceder("sites", "view"));
        definirVisibilite(boutonCampagnes, session.peutAcceder("campagnes", "view"));
        definirVisibilite(boutonSuivis, session.peutAcceder("suivis", "view"));
        definirVisibilite(boutonEssences, session.peutAcceder("essences", "view"));
        definirVisibilite(boutonObjectifs, session.peutAcceder("objectifs", "view"));
        definirVisibilite(boutonFinances, session.peutAcceder("finances", "view"));
        definirVisibilite(boutonIntelligence, session.peutAcceder("intelligence", "view"));
        definirVisibilite(boutonUtilisateurs, estAdmin);
        definirVisibilite(boutonJournal, estAdmin);

        definirVisibilite(groupeTerrain, boutonSites.isVisible() || boutonCampagnes.isVisible() || boutonSuivis.isVisible() || boutonEssences.isVisible());
        definirVisibilite(groupeProgramme, boutonObjectifs.isVisible() || boutonFinances.isVisible() || boutonIntelligence.isVisible());
        definirVisibilite(groupeAdministration, estAdmin);
    }

    private void definirVisibilite(javafx.scene.Node noeud, boolean visible) {
        noeud.setVisible(visible);
        noeud.setManaged(visible);
    }

    private void activerNavigation(Button boutonActif) {
        for (Destination destination : destinations.values()) {
            if (destination.bouton() != null) {
                destination.bouton().getStyleClass().removeAll("bouton-nav-actif", "active");
            }
        }
        if (boutonActif != null) {
            boutonActif.getStyleClass().add("active");
        }
    }

    @FXML private void allerDashboard() { aller(Navigation.Ecran.TABLEAU_DE_BORD, null); }
    @FXML private void allerCarte() { aller(Navigation.Ecran.CARTE, null); }
    @FXML private void allerSites() { aller(Navigation.Ecran.SITES, null); }
    @FXML private void allerCampagnes() { aller(Navigation.Ecran.CAMPAGNES, null); }
    @FXML private void allerSuivis() { aller(Navigation.Ecran.SUIVIS, null); }
    @FXML private void allerEssences() { aller(Navigation.Ecran.ESSENCES, null); }
    @FXML private void allerObjectifs() { aller(Navigation.Ecran.OBJECTIFS, null); }
    @FXML private void allerFinances() { aller(Navigation.Ecran.FINANCES, null); }
    @FXML private void allerIntelligence() { aller(Navigation.Ecran.INTELLIGENCE, null); }
    @FXML private void allerUtilisateurs() { aller(Navigation.Ecran.UTILISATEURS, null); }
    @FXML private void allerJournal() { aller(Navigation.Ecran.JOURNAL, null); }
    @FXML private void allerNotifications() { aller(Navigation.Ecran.NOTIFICATIONS, null); }
    @FXML private void allerParametres() { aller(Navigation.Ecran.PARAMETRES, null); }

    @FXML
    private void seDeconnecter() {
        if (minuteurNotifications != null) {
            minuteurNotifications.stop();
        }
        Navigation.definirHote(null);
        Navigation.definirDeconnexion(null);
        SessionManager.getInstance().vider();
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/login.fxml");
    }
}
