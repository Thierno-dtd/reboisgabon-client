package com.reboisgabon.client.ui;

import javafx.scene.text.Font;

import java.io.InputStream;

public final class Polices {

    private static final String[] FICHIERS = {
            "PublicSans-Regular.ttf",
            "PublicSans-Medium.ttf",
            "PublicSans-SemiBold.ttf",
            "PublicSans-Bold.ttf",
            "Archivo-SemiBold.ttf",
            "Archivo-Bold.ttf",
            "Archivo-ExtraBold.ttf",
            "Spectral-Italic.ttf",
            "Spectral-MediumItalic.ttf"
    };

    private static boolean chargees;

    private Polices() {
    }

    public static synchronized void charger() {
        if (chargees) {
            return;
        }
        for (String fichier : FICHIERS) {
            try (InputStream flux = Polices.class.getResourceAsStream("/com/reboisgabon/client/fonts/" + fichier)) {
                if (flux != null) {
                    Font.loadFont(flux, 13);
                }
            } catch (Exception ignore) {
            }
        }
        chargees = true;
    }
}
