package com.reboisgabon.client.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public final class FiltreAutoUtil {

    private FiltreAutoUtil() {
    }

    public static void surSaisie(TextField champ, Runnable action) {
        Timeline temporisateur = new Timeline();
        champ.textProperty().addListener((observable, ancien, nouveau) -> {
            temporisateur.stop();
            temporisateur.getKeyFrames().setAll(new KeyFrame(Duration.millis(400), evenement -> action.run()));
            temporisateur.play();
        });
    }

    public static void surValeur(ComboBoxBase<?> champ, Runnable action) {
        champ.valueProperty().addListener((observable, ancien, nouveau) -> action.run());
    }
}