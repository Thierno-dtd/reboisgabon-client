package com.reboisgabon.client.util;

import com.reboisgabon.client.ui.Icones;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;

import java.util.Optional;

public final class AlertUtil {

    private static final String THEME = "/com/reboisgabon/client/theme/theme.css";

    private AlertUtil() {
    }

    private static Alert creer(Alert.AlertType type, String titre, String message, String icone, Color couleur) {
        Alert alerte = new Alert(type);
        alerte.setTitle(titre);
        alerte.setHeaderText(titre);
        alerte.setContentText(message);
        alerte.setGraphic(Icones.icone(icone, 28, couleur));
        alerte.getDialogPane().getStylesheets().add(AlertUtil.class.getResource(THEME).toExternalForm());
        alerte.getDialogPane().setMinWidth(420);
        if (SceneNavigator.getInstance().getStagePrincipal() != null
                && SceneNavigator.getInstance().getStagePrincipal().isShowing()) {
            alerte.initOwner(SceneNavigator.getInstance().getStagePrincipal());
        }
        return alerte;
    }

    public static void erreur(String titre, String message) {
        Alert alerte = creer(Alert.AlertType.ERROR, titre, message, Icones.ALERTE, Color.web("#B3412E"));
        alerte.getDialogPane().getStyleClass().add("alerte-erreur");
        alerte.showAndWait();
    }

    public static void information(String titre, String message) {
        creer(Alert.AlertType.INFORMATION, titre, message, Icones.VALIDE, Color.web("#1F5136")).showAndWait();
    }

    public static boolean confirmation(String titre, String message) {
        Alert alerte = creer(Alert.AlertType.CONFIRMATION, titre, message, Icones.INFO, Color.web("#2E6A9E"));
        ButtonType oui = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType non = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alerte.getButtonTypes().setAll(non, oui);
        Optional<ButtonType> resultat = alerte.showAndWait();
        return resultat.isPresent() && resultat.get() == oui;
    }
}
