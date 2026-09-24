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
            for (javafx.scene.Node entete : vue.lookupAll(".entete-page")) {
                for (javafx.scene.Node n : entete.lookupAll(".button")) {
                    if (n instanceof javafx.scene.control.Button bouton) {
                        bouton.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}