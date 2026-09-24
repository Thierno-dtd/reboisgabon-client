package com.reboisgabon.client.ui;

import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public final class Icones {

    public static final String TABLEAU_DE_BORD = "mdi2v-view-dashboard-outline";
    public static final String CARTE = "mdi2m-map-outline";
    public static final String SITE = "mdi2m-map-marker-outline";
    public static final String CAMPAGNE = "mdi2s-sprout-outline";
    public static final String SUIVI = "mdi2c-clipboard-text-outline";
    public static final String ESSENCE = "mdi2p-pine-tree";
    public static final String ARBRE = "mdi2t-tree-outline";
    public static final String OBJECTIF = "mdi2f-flag-outline";
    public static final String FINANCES = "mdi2b-bank-outline";
    public static final String PARTENAIRE = "mdi2h-handshake-outline";
    public static final String BUDGET = "mdi2w-wallet-outline";
    public static final String INTELLIGENCE = "mdi2f-flask-outline";
    public static final String UTILISATEURS = "mdi2a-account-group-outline";
    public static final String JOURNAL = "mdi2h-history";
    public static final String PARAMETRES = "mdi2c-cog-outline";
    public static final String NOTIFICATIONS = "mdi2b-bell-outline";
    public static final String DECONNEXION = "mdi2l-logout-variant";
    public static final String PDF = "mdi2f-file-pdf-outline";
    public static final String EXCEL = "mdi2m-microsoft-excel";
    public static final String RECHERCHE = "mdi2m-magnify";
    public static final String MODIFIER = "mdi2p-pencil-outline";
    public static final String SUPPRIMER = "mdi2d-delete-outline";
    public static final String AJOUTER = "mdi2p-plus";
    public static final String FERMER = "mdi2c-close";
    public static final String ALERTE = "mdi2a-alert-outline";
    public static final String INFO = "mdi2i-information-outline";
    public static final String VALIDE = "mdi2c-check-circle-outline";
    public static final String SURFACE = "mdi2r-ruler-square";
    public static final String CALENDRIER = "mdi2c-calendar-month-outline";
    public static final String PHOTO = "mdi2i-image-outline";
    public static final String SCORE = "mdi2l-leaf";
    public static final String COMPTE = "mdi2a-account-outline";
    public static final String SECURITE = "mdi2s-shield-lock-outline";
    public static final String CLE = "mdi2k-key-outline";
    public static final String DEUX_FACTEURS = "mdi2t-two-factor-authentication";
    public static final String EMAIL = "mdi2e-email-outline";
    public static final String OEIL = "mdi2e-eye-outline";
    public static final String OEIL_BARRE = "mdi2e-eye-off-outline";
    public static final String HAUSSE = "mdi2t-trending-up";
    public static final String BAISSE = "mdi2t-trending-down";
    public static final String CHEVRON = "mdi2c-chevron-right";
    public static final String RECENTRER = "mdi2c-crosshairs";
    public static final String COUCHES = "mdi2l-layers-outline";
    public static final String PROXIMITE = "mdi2m-map-marker-radius-outline";
    public static final String HORLOGE = "mdi2c-clock-outline";
    public static final String LISTE = "mdi2f-format-list-bulleted";
    public static final String RAFRAICHIR = "mdi2r-refresh";
    public static final String FILTRE = "mdi2f-filter-variant";
    public static final String DESACTIVER = "mdi2a-account-cancel-outline";
    public static final String ACTIVER = "mdi2a-account-check-outline";
    public static final String LU = "mdi2c-check";
    public static final String SAISON = "mdi2c-calendar-range";

    private Icones() {
    }

    public static FontIcon icone(String litteral) {
        FontIcon icone = new FontIcon(litteral);
        icone.getStyleClass().add("ikonli-font-icon");
        return icone;
    }

    public static FontIcon icone(String litteral, int taille) {
        FontIcon icone = icone(litteral);
        icone.setIconSize(taille);
        return icone;
    }

    public static FontIcon icone(String litteral, int taille, Color couleur) {
        FontIcon icone = icone(litteral, taille);
        icone.setIconColor(couleur);
        return icone;
    }
}
