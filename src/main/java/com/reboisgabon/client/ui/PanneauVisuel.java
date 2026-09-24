package com.reboisgabon.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.net.URL;

public class PanneauVisuel extends StackPane {

    private MediaView vueVideo;
    private MediaPlayer lecteur;
    private final StackPane calqueVideo = new StackPane();

    public PanneauVisuel() {
        getStyleClass().add("connexion-panneau-visuel");
        setMinWidth(420);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        Rectangle voile = new Rectangle();
        voile.widthProperty().bind(widthProperty());
        voile.heightProperty().bind(heightProperty());
        voile.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#12281B", 0.72)), new Stop(0.55, Color.web("#12281B", 0.55)), new Stop(1, Color.web("#0D1F15", 0.94))));
        voile.setMouseTransparent(true);

        Illustrations.CourbesNiveau courbes = new Illustrations.CourbesNiveau(Color.web("#DCEAD5"), 0.10, 13);
        Illustrations.Canopee canopee = new Illustrations.Canopee(Color.web("#0A1810"), 120, 4);
        VBox decor = new VBox(courbes, canopee);
        VBox.setVgrow(courbes, Priority.ALWAYS);
        decor.setMouseTransparent(true);

        HBox feuilles = new HBox(-10,
                Illustrations.feuille(Illustrations.Espece.MOABI, 170, Color.web("#DCEAD5", 0.75), Color.web("#1F5136", 0.55)),
                Illustrations.feuille(Illustrations.Espece.OKOUME, 240, Color.web("#DCEAD5", 0.8), Color.web("#1F5136", 0.55)));
        feuilles.setAlignment(Pos.BOTTOM_RIGHT);
        feuilles.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        feuilles.setMouseTransparent(true);
        StackPane.setAlignment(feuilles, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(feuilles, new Insets(0, 36, 70, 0));

        Label nom = new Label("ReboisGabon");
        nom.getStyleClass().add("chiffre-visuel");
        Label sous = new Label("Pilotage du reboisement");
        sous.getStyleClass().add("libelle-visuel");
        HBox marque = new HBox(12, Illustrations.logo(42), new VBox(0, nom, sous));
        marque.setAlignment(Pos.CENTER_LEFT);

        Label accroche = new Label("Suivre chaque plant, de la mise en terre à la canopée.");
        accroche.getStyleClass().add("accroche");
        accroche.setWrapText(true);
        accroche.setMaxWidth(470);
        Label texte = new Label("Sites, campagnes de plantation, contrôles de survie et financements des programmes de reboisement, réunis sur la carte des neuf provinces du Gabon.");
        texte.getStyleClass().add("texte-visuel");
        texte.setWrapText(true);
        texte.setMaxWidth(440);

        VBox atouts = new VBox(12,
                atout(Icones.CARTE, "Carte des provinces et fiche détaillée de chaque site"),
                atout(Icones.SCORE, "Taux de survie calculé à chaque contrôle de terrain"),
                atout(Icones.PDF, "Rapports PDF et Excel prêts pour les partenaires"));

        VBox message = new VBox(18, accroche, texte, atouts);
        Region espace = new Region();
        VBox.setVgrow(espace, Priority.ALWAYS);
        Label pied = new Label("© 2026 ReboisGabon  ·  Accès réservé aux comptes habilités");
        pied.getStyleClass().add("libelle-visuel");
        VBox contenu = new VBox(28, marque, espace, message, new Region(), pied);
        VBox.setMargin(pied, new Insets(40, 0, 0, 0));
        contenu.setPadding(new Insets(40, 48, 32, 48));
        contenu.setPickOnBounds(false);

        getChildren().addAll(calqueVideo, voile, decor, feuilles, contenu);
    }

    private HBox atout(String icone, String texte) {
        StackPane pastille = new StackPane(Icones.icone(icone, 17, Color.web("#C99A1C")));
        pastille.setMinSize(32, 32);
        pastille.setMaxSize(32, 32);
        pastille.setStyle("-fx-background-color: rgba(201,154,28,0.14); -fx-background-radius: 8;");
        Label l = new Label(texte);
        l.getStyleClass().add("texte-visuel");
        l.setStyle("-fx-text-fill: #E4EFDE;");
        HBox ligne = new HBox(12, pastille, l);
        ligne.setAlignment(Pos.CENTER_LEFT);
        return ligne;
    }

    public boolean isVideo() {
        return lecteur != null;
    }

    public void setVideo(boolean active) {
        if (!active || lecteur != null) {
            return;
        }
        try {
            URL ressource = getClass().getResource("/com/reboisgabon/client/media/rebois-login.mp4");
            if (ressource == null) {
                return;
            }
            lecteur = new MediaPlayer(new Media(ressource.toExternalForm()));
            lecteur.setCycleCount(MediaPlayer.INDEFINITE);
            lecteur.setMute(true);
            lecteur.setAutoPlay(true);
            lecteur.setOnReady(this::requestLayout);
            vueVideo = new MediaView(lecteur);
            vueVideo.setPreserveRatio(true);
            vueVideo.setMouseTransparent(true);
            calqueVideo.getChildren().setAll(vueVideo);
            sceneProperty().addListener((o, ancienne, nouvelle) -> {
                if (nouvelle == null && lecteur != null) {
                    lecteur.stop();
                    lecteur.dispose();
                    lecteur = null;
                }
            });
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (vueVideo != null && lecteur != null && lecteur.getMedia().getWidth() > 0) {
            double ratio = (double) lecteur.getMedia().getWidth() / lecteur.getMedia().getHeight();
            double largeur = Math.max(getWidth(), getHeight() * ratio);
            vueVideo.setFitWidth(largeur);
        } else if (vueVideo != null) {
            vueVideo.setFitHeight(getHeight());
        }
    }
}
