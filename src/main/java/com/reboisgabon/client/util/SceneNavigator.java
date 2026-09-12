package com.reboisgabon.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneNavigator {

    private static final SceneNavigator INSTANCE = new SceneNavigator();

    private Stage stagePrincipal;
    private Scene scenePrincipale;

    private SceneNavigator() {
    }

    public static SceneNavigator getInstance() {
        return INSTANCE;
    }

    public void initialiser(Stage stage) {
        this.stagePrincipal = stage;
    }

    public void naviguerVers(String cheminFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(cheminFxml));
            Parent racine = loader.load();
            if (scenePrincipale == null) {
                scenePrincipale = new Scene(racine, 1280, 800);
                scenePrincipale.getStylesheets().add(
                        getClass().getResource("/com/reboisgabon/client/theme/theme.css").toExternalForm());
                stagePrincipal.setScene(scenePrincipale);
            } else {
                scenePrincipale.setRoot(racine);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Stage getStagePrincipal() {
        return stagePrincipal;
    }
}