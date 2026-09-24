package com.reboisgabon.client.util;

import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.Icones;
import javafx.scene.control.Button;

public final class BoutonIconeUtil {

    private BoutonIconeUtil() {
    }

    public static Button creer(String symbole, String infoBulle, String variante) {
        return Composants.boutonIcone(icone(symbole, infoBulle), infoBulle, variante);
    }

    public static Button creer(String symbole, String infoBulle) {
        return creer(symbole, infoBulle, null);
    }

    private static String icone(String symbole, String infoBulle) {
        if (symbole != null && symbole.startsWith("mdi2")) {
            return symbole;
        }
        String cle = infoBulle == null ? "" : infoBulle.toLowerCase();
        if (cle.contains("modifier")) return Icones.MODIFIER;
        if (cle.contains("supprimer")) return Icones.SUPPRIMER;
        if (cle.contains("score")) return Icones.SCORE;
        if (cle.contains("photo")) return Icones.PHOTO;
        if (cle.contains("désactiver")) return Icones.DESACTIVER;
        if (cle.contains("réactiver")) return Icones.ACTIVER;
        if (cle.contains("lu")) return Icones.LU;
        return Icones.CHEVRON;
    }
}
