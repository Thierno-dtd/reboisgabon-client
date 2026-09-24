package com.reboisgabon.client.controllers.notifications;

import com.reboisgabon.client.api.endpoints.NotificationsApi;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.notifications.Notification;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.Icones;
import com.reboisgabon.client.ui.Navigation;
import com.reboisgabon.client.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class NotificationsController implements Initializable {

    @FXML private VBox conteneurNotifications;

    private final NotificationsApi notificationsApi = new NotificationsApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        charger();
    }

    private void charger() {
        new Thread(() -> {
            try {
                PageDrf<Notification> page = notificationsApi.lister();
                Platform.runLater(() -> afficher(page));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Chargement impossible", "Les notifications n'ont pas pu être chargées."));
            }
        }).start();
    }

    private void afficher(PageDrf<Notification> page) {
        conteneurNotifications.getChildren().clear();
        if (page.getResults() == null || page.getResults().isEmpty()) {
            conteneurNotifications.getChildren().add(Composants.etatVide("Aucune notification", "Les alertes de survie et de contrôles en retard apparaîtront ici."));
            return;
        }
        VBox feuille = new VBox();
        feuille.getStyleClass().add("feuille");
        for (Notification n : page.getResults()) {
            feuille.getChildren().add(ligne(n));
        }
        Label total = new Label(page.getCount() + " notifications au total, les plus récentes en premier.");
        total.getStyleClass().add("texte-muet");
        conteneurNotifications.getChildren().addAll(total, feuille);
    }

    private HBox ligne(Notification n) {
        String type = n.getTypeNotification() == null ? "" : n.getTypeNotification();
        String icone;
        String couleur;
        String fond;
        switch (type) {
            case "TAUX_CRITIQUE" -> { icone = Icones.ALERTE; couleur = "#B3412E"; fond = "#F8E3DE"; }
            case "SUIVI_EN_RETARD" -> { icone = Icones.HORLOGE; couleur = "#9A5B34"; fond = "#F4E6DB"; }
            case "SUIVI_A_VENIR" -> { icone = Icones.CALENDRIER; couleur = "#2E6A9E"; fond = "#E2EDF6"; }
            case "NOUVEAU_FINANCEMENT" -> { icone = Icones.FINANCES; couleur = "#8F6B0C"; fond = "#F7EDCD"; }
            default -> { icone = Icones.COMPTE; couleur = "#1F5136"; fond = "#E4EFDE"; }
        }
        StackPane pastille = new StackPane(Icones.icone(icone, 19, Color.web(couleur)));
        pastille.setMinSize(38, 38);
        pastille.setMaxSize(38, 38);
        pastille.setStyle("-fx-background-color: " + fond + "; -fx-background-radius: 8;");

        Label titre = new Label(n.getTitre() != null ? n.getTitre() : "Notification");
        titre.getStyleClass().add("titre-3");
        if (n.isLue()) {
            titre.setStyle("-fx-font-weight: normal;");
        }
        Label message = new Label(n.getMessage());
        message.getStyleClass().add("texte-corps");
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #46594B;");
        Label quand = new Label(ilYa(n.getCreatedAt()));
        quand.getStyleClass().add("texte-petit");
        VBox textes = new VBox(3, titre, message, quand);
        HBox.setHgrow(textes, Priority.ALWAYS);
        textes.setMinWidth(0);

        HBox ligne = new HBox(14, pastille, textes);
        ligne.setAlignment(Pos.TOP_LEFT);
        ligne.getStyleClass().add("ligne-liste");
        ligne.setPadding(new Insets(14, 16, 14, 16));
        if (!n.isLue()) {
            ligne.setStyle("-fx-background-color: #FBFCF9;");
            Circle point = new Circle(4.5, Color.web("#C99A1C"));
            Button marquer = Composants.bouton("Marquer comme lue", Icones.LU, "bouton-lien");
            marquer.setOnAction(e -> marquerLue(n.getId()));
            VBox droite = new VBox(8, point, marquer);
            droite.setAlignment(Pos.TOP_RIGHT);
            ligne.getChildren().add(droite);
        }
        if ("CampagnePlantation".equals(n.getLienModele())) {
            Button voir = Composants.bouton("Voir les suivis", Icones.CHEVRON, "bouton-lien");
            voir.setOnAction(e -> Navigation.aller(Navigation.Ecran.SUIVIS));
            textes.getChildren().add(voir);
            voir.setPadding(new Insets(2, 0, 0, 0));
        }
        return ligne;
    }

    private String ilYa(OffsetDateTime date) {
        if (date == null) {
            return "";
        }
        Duration ecart = Duration.between(date, OffsetDateTime.now());
        if (ecart.toMinutes() < 1) {
            return "à l'instant";
        }
        if (ecart.toHours() < 1) {
            return "il y a " + ecart.toMinutes() + " min";
        }
        if (ecart.toDays() < 1) {
            return "il y a " + ecart.toHours() + " h";
        }
        if (ecart.toDays() < 7) {
            return "il y a " + ecart.toDays() + " jour" + (ecart.toDays() > 1 ? "s" : "");
        }
        return "le " + date.format(DateTimeFormatter.ofPattern("d MMMM yyyy à HH:mm", Locale.FRANCE));
    }

    private void marquerLue(String id) {
        new Thread(() -> {
            try {
                notificationsApi.marquerLue(id);
                Platform.runLater(this::charger);
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Action impossible", "La notification n'a pas pu être marquée comme lue."));
            }
        }).start();
    }

    @FXML
    private void toutMarquerLu() {
        new Thread(() -> {
            try {
                notificationsApi.marquerToutesLues();
                Platform.runLater(this::charger);
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Action impossible", "Les notifications n'ont pas pu être marquées comme lues."));
            }
        }).start();
    }
}
