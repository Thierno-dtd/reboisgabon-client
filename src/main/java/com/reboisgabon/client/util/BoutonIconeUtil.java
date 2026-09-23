package com.reboisgabon.client.util;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public final class BoutonIconeUtil {

    private BoutonIconeUtil() {
    }

    public static Button creer(String emoji, String infoBulle, String variante) {
        Button bouton = new Button(emoji);
        bouton.getStyleClass().add("bouton-icone");
        if (variante != null && !variante.isBlank()) {
            bouton.getStyleClass().add(variante);
        }
        bouton.setTooltip(new Tooltip(infoBulle));
        return bouton;
    }

    public static Button creer(String emoji, String infoBulle) {
        return creer(emoji, infoBulle, null);
    }
}