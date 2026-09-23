package com.reboisgabon.client.controllers;

import com.reboisgabon.client.api.endpoints.NotificationsApi;
import com.reboisgabon.client.session.Role;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.ContentHost;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class ShellController implements Initializable {

    @FXML
    private VBox sidebar;

    @FXML
    private Button toggleSidebarBtn;

    @FXML
    private Label logoLabel;

    @FXML
    private StackPane zoneContenu;

    @FXML
    private Label libelleTitreEcran;

    @FXML
    private Label libelleNomUtilisateur;

    @FXML
    private Label libelleRoleUtilisateur;

    @FXML
    private Button boutonDashboard;

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

    @FXML
    private Label badgeNotifications;

    @FXML
    private Button boutonExports;

    @FXML
    private Button boutonParametres;

    private final NotificationsApi notificationsApi = new NotificationsApi();

    private boolean isExpanded = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ContentHost.getInstance().definirConteneur(zoneContenu);

        var utilisateur = SessionManager.getInstance().getUtilisateurConnecte();

        if (utilisateur != null) {
            if (libelleNomUtilisateur != null) {
                libelleNomUtilisateur.setText(utilisateur.getNomComplet());
            }

            if (libelleRoleUtilisateur != null && utilisateur.getRole() != null) {
                libelleRoleUtilisateur.setText(utilisateur.getRole().toString());
            }
        }

        appliquerVisibilitePermissions();
        allerDashboard();
        demarrerRafraichissementNotifications();
    }

    @FXML
    private void handleToggleSidebar() {
        isExpanded = !isExpanded;

        if (isExpanded) {
            sidebar.setPrefWidth(240);

            if (logoLabel != null) {
                logoLabel.setVisible(true);
                logoLabel.setManaged(true);
            }

            toggleSidebarBtn.setText("☰");

            definirTexteBoutons(
                    "📊  Dashboard",
                    "📍  Sites",
                    "🌱  Campagnes",
                    "📈  Suivis",
                    "🌳  Essences",
                    "🎯  Objectifs",
                    "💰  Finances",
                    "🤖  IA Ecologique",
                    "👥  Utilisateurs",
                    "📜  Journal"
            );

        } else {
            sidebar.setPrefWidth(72);

            if (logoLabel != null) {
                logoLabel.setVisible(false);
                logoLabel.setManaged(false);
            }

            toggleSidebarBtn.setText("☰");

            definirTexteBoutons(
                    "📊",
                    "📍",
                    "🌱",
                    "📈",
                    "🌳",
                    "🎯",
                    "💰",
                    "🤖",
                    "👥",
                    "📜"
            );
        }
    }

    private void definirTexteBoutons(
            String dashboard,
            String sites,
            String campagnes,
            String suivis,
            String essences,
            String objectifs,
            String finances,
            String intelligence,
            String utilisateurs,
            String journal
    ) {
        if (boutonDashboard != null) boutonDashboard.setText(dashboard);
        if (boutonSites != null) boutonSites.setText(sites);
        if (boutonCampagnes != null) boutonCampagnes.setText(campagnes);
        if (boutonSuivis != null) boutonSuivis.setText(suivis);
        if (boutonEssences != null) boutonEssences.setText(essences);
        if (boutonObjectifs != null) boutonObjectifs.setText(objectifs);
        if (boutonFinances != null) boutonFinances.setText(finances);
        if (boutonIntelligence != null) boutonIntelligence.setText(intelligence);
        if (boutonUtilisateurs != null) boutonUtilisateurs.setText(utilisateurs);
        if (boutonJournal != null) boutonJournal.setText(journal);
    }

    private void demarrerRafraichissementNotifications() {
        rafraichirBadgeNotifications();

        Timeline minuteur = new Timeline(
                new KeyFrame(
                        Duration.seconds(60),
                        evenement -> rafraichirBadgeNotifications()
                )
        );

        minuteur.setCycleCount(Timeline.INDEFINITE);
        minuteur.play();
    }

    private void rafraichirBadgeNotifications() {
        Thread thread = new Thread(() -> {
            try {
                int nombre = notificationsApi.compterNonLues();

                Platform.runLater(() -> {
                    if (badgeNotifications == null) {
                        return;
                    }

                    if (nombre > 0) {
                        badgeNotifications.setText(String.valueOf(nombre));
                        badgeNotifications.setVisible(true);
                        badgeNotifications.setManaged(true);
                    } else {
                        badgeNotifications.setVisible(false);
                        badgeNotifications.setManaged(false);
                    }
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

        definirVisibilite(
                boutonSites,
                session.peutAcceder("sites", "view")
        );

        definirVisibilite(
                boutonCampagnes,
                session.peutAcceder("campagnes", "view")
        );

        definirVisibilite(
                boutonSuivis,
                session.peutAcceder("suivis", "view")
        );

        definirVisibilite(
                boutonEssences,
                session.peutAcceder("essences", "view")
        );

        definirVisibilite(
                boutonObjectifs,
                session.peutAcceder("objectifs", "view")
        );

        definirVisibilite(
                boutonFinances,
                session.peutAcceder("finances", "view")
        );

        definirVisibilite(
                boutonIntelligence,
                session.peutAcceder("intelligence", "view")
        );

        definirVisibilite(
                boutonUtilisateurs,
                estAdmin
        );

        definirVisibilite(
                boutonJournal,
                estAdmin
        );
    }

    private void definirVisibilite(Button bouton, boolean visible) {
        if (bouton == null) {
            return;
        }

        bouton.setVisible(visible);
        bouton.setManaged(visible);
    }

    private void activerNavigation(Button boutonActif) {
        Button[] boutons = {
                boutonDashboard,
                boutonSites,
                boutonCampagnes,
                boutonSuivis,
                boutonEssences,
                boutonObjectifs,
                boutonFinances,
                boutonIntelligence,
                boutonUtilisateurs,
                boutonJournal,
                boutonExports,
                boutonParametres
        };

        for (Button bouton : boutons) {
            if (bouton != null) {
                bouton.getStyleClass().remove("bouton-nav-actif");
                bouton.getStyleClass().remove("active");
            }
        }

        if (boutonActif != null) {
            boutonActif.getStyleClass().add("bouton-nav-actif");
            boutonActif.getStyleClass().add("active");
        }
    }

    @FXML
    private void allerDashboard() {
        activerNavigation(boutonDashboard);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Tableau de bord");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/dashboard.fxml"
        );
    }

    @FXML
    private void allerSites() {
        activerNavigation(boutonSites);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Sites de reboisement");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/sites.fxml"
        );
    }

    @FXML
    private void allerCampagnes() {
        activerNavigation(boutonCampagnes);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Campagnes de plantation");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/campagnes.fxml"
        );
    }

    @FXML
    private void allerSuivis() {
        activerNavigation(boutonSuivis);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Suivis de croissance");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/suivis.fxml"
        );
    }

    @FXML
    private void allerEssences() {
        activerNavigation(boutonEssences);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Essences");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/essences.fxml"
        );
    }

    @FXML
    private void allerObjectifs() {
        activerNavigation(boutonObjectifs);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Objectifs de reboisement");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/objectifs.fxml"
        );
    }

    @FXML
    private void allerFinances() {
        activerNavigation(boutonFinances);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Finances");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/finances.fxml"
        );
    }

    @FXML
    private void allerIntelligence() {
        activerNavigation(boutonIntelligence);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Intelligence écologique");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/intelligence.fxml"
        );
    }

    @FXML
    private void allerUtilisateurs() {
        activerNavigation(boutonUtilisateurs);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Utilisateurs");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/utilisateurs.fxml"
        );
    }

    @FXML
    private void allerJournal() {
        activerNavigation(boutonJournal);

        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Journal d'activité");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/journal.fxml"
        );
    }

    @FXML
    private void allerNotifications() {
        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Notifications");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/notifications.fxml"
        );
    }

    @FXML
    private void allerParametres() {
        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Paramètres du compte");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/parametres.fxml"
        );
    }

    @FXML
    private void allerExports() {
        if (libelleTitreEcran != null) {
            libelleTitreEcran.setText("Exports");
        }

        ContentHost.getInstance().afficher(
                "/com/reboisgabon/client/fxml/exports.fxml"
        );
    }

    @FXML
    private void seDeconnecter() {
        SessionManager.getInstance().vider();

        SceneNavigator.getInstance().naviguerVers(
                "/com/reboisgabon/client/fxml/login.fxml"
        );
    }
}