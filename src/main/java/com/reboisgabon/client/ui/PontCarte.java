package com.reboisgabon.client.ui;

import javafx.application.Platform;

import java.util.function.Consumer;

public class PontCarte {

    private final Consumer<String> surSite;
    private final Consumer<String> surProvince;

    public PontCarte(Consumer<String> surSite, Consumer<String> surProvince) {
        this.surSite = surSite;
        this.surProvince = surProvince;
    }

    public void ouvrirSite(String id) {
        Platform.runLater(() -> surSite.accept(id));
    }

    public void ouvrirProvince(String nom) {
        Platform.runLater(() -> surProvince.accept(nom));
    }

    public void journal(String message) {
        System.err.println("[carte] " + message);
    }
}
