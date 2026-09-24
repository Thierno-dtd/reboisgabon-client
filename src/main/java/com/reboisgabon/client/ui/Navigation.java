package com.reboisgabon.client.ui;

import java.util.function.Consumer;

public final class Navigation {

    public enum Ecran {
        TABLEAU_DE_BORD, CARTE, SITES, CAMPAGNES, SUIVIS, ESSENCES, OBJECTIFS, FINANCES, INTELLIGENCE, UTILISATEURS, JOURNAL, PARAMETRES, NOTIFICATIONS
    }

    public interface Hote {
        <T> void aller(Ecran ecran, Consumer<T> configuration);
    }

    private static Hote hote;
    private static Runnable deconnexion;

    private Navigation() {
    }

    public static void definirHote(Hote nouvelHote) {
        hote = nouvelHote;
    }

    public static void definirDeconnexion(Runnable action) {
        deconnexion = action;
    }

    public static void deconnecter() {
        if (deconnexion != null) {
            deconnexion.run();
        }
    }

    public static void aller(Ecran ecran) {
        aller(ecran, null);
    }

    public static <T> void aller(Ecran ecran, Consumer<T> configuration) {
        if (hote != null) {
            hote.aller(ecran, configuration);
        }
    }
}
