package com.reboisgabon.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.function.Consumer;

public final class ContentHost {

    private static final ContentHost INSTANCE = new ContentHost();

    private StackPane conteneur;

    private ContentHost() {
    }

    public static ContentHost getInstance() {
        return INSTANCE;
    }

    public void definirConteneur(StackPane conteneur) {
        this.conteneur = conteneur;
    }

    public void afficher(String cheminFxml) {
        afficher(cheminFxml, null);
    }

    public <T> void afficher(String cheminFxml, Consumer<T> configurerControleur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(cheminFxml));
            Parent vue = loader.load();
            if (configurerControleur != null) {
                T controleur = loader.getController();
                configurerControleur.accept(controleur);
            }
            conteneur.getChildren().setAll(vue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}