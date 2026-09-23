package com.reboisgabon.client.controllers.notifications;

import com.reboisgabon.client.api.endpoints.NotificationsApi;
import com.reboisgabon.client.dto.notifications.Notification;
import com.reboisgabon.client.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import com.reboisgabon.client.util.BoutonIconeUtil;

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
                var page = notificationsApi.lister();
                Platform.runLater(() -> afficher(page.getResults()));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les notifications."));
            }
        }).start();
    }

    private void afficher(List<Notification> notifications) {
        conteneurNotifications.getChildren().clear();
        for (Notification notification : notifications) {
            HBox ligne = new HBox(14);
            ligne.getStyleClass().add(notification.isLue() ? "carte-verre" : "carte-verre-forte");
            ligne.setPadding(new Insets(14));
            Label libelleMessage = new Label(notification.getMessage());
            libelleMessage.getStyleClass().add("texte-corps");
            libelleMessage.setWrapText(true);
            Region espaceur = new Region();
            HBox.setHgrow(espaceur, javafx.scene.layout.Priority.ALWAYS);
            ligne.getChildren().addAll(libelleMessage, espaceur);
            if (!notification.isLue()) {
                Button boutonMarquer = BoutonIconeUtil.creer("✅", "Marquer comme lu", "bouton-icone-succes");
                boutonMarquer.setOnAction(evenement -> marquerLue(notification.getId()));
                ligne.getChildren().add(boutonMarquer);
            }
            conteneurNotifications.getChildren().add(ligne);
        }
    }

    private void marquerLue(String id) {
        new Thread(() -> {
            try {
                notificationsApi.marquerLue(id);
                Platform.runLater(this::charger);
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de marquer la notification comme lue."));
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
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de marquer les notifications comme lues."));
            }
        }).start();
    }
}