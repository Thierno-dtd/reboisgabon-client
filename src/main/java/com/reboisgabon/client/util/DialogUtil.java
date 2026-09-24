package com.reboisgabon.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public final class DialogUtil {

    private DialogUtil() {
    }

    public static <T> void ouvrirModal(String cheminFxml, String titre, Consumer<T> configurerControleur) {
        try {
            FXMLLoader loader = new FXMLLoader(DialogUtil.class.getResource(cheminFxml));
            Parent racine = loader.load();
            T controleur = loader.getController();
            if (configurerControleur != null) {
                configurerControleur.accept(controleur);
            }
            Stage fenetreModale = new Stage();
            fenetreModale.setTitle(titre);
            fenetreModale.initModality(Modality.APPLICATION_MODAL);
            Stage principal = SceneNavigator.getInstance().getStagePrincipal();
            if (principal != null && principal.isShowing()) {
                fenetreModale.initOwner(principal);
                fenetreModale.getIcons().setAll(principal.getIcons());
            }
            if (!racine.getStyleClass().contains("fond-application")) {
                racine.getStyleClass().add("fond-application");
            }
            Scene scene = new Scene(racine);
            scene.getStylesheets().add(DialogUtil.class.getResource("/com/reboisgabon/client/theme/theme.css").toExternalForm());
            fenetreModale.setScene(scene);
            fenetreModale.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}