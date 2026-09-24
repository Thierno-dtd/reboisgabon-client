package com.reboisgabon.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class Composants {

    private static final DecimalFormatSymbols SYMBOLES = new DecimalFormatSymbols(Locale.FRANCE);
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRANCE);

    static {
        SYMBOLES.setGroupingSeparator(' ');
    }

    private Composants() {
    }

    public static String nombre(Number valeur) {
        if (valeur == null) {
            return "—";
        }
        return new DecimalFormat("#,##0", SYMBOLES).format(valeur);
    }

    public static String decimal(Number valeur) {
        if (valeur == null) {
            return "—";
        }
        return new DecimalFormat("#,##0.#", SYMBOLES).format(valeur);
    }

    public static String pourcentage(Number valeur) {
        if (valeur == null) {
            return "—";
        }
        return new DecimalFormat("0.0", SYMBOLES).format(valeur) + " %";
    }

    public static String montant(Number valeur) {
        if (valeur == null) {
            return "—";
        }
        double v = valeur.doubleValue();
        if (Math.abs(v) >= 1_000_000_000) {
            return new DecimalFormat("0.00", SYMBOLES).format(v / 1_000_000_000) + " Md FCFA";
        }
        if (Math.abs(v) >= 1_000_000) {
            return new DecimalFormat("0.0", SYMBOLES).format(v / 1_000_000) + " M FCFA";
        }
        return nombre(v) + " FCFA";
    }

    public static String date(LocalDate date) {
        return date == null ? "—" : FORMAT_DATE.format(date);
    }

    public static String date(String iso) {
        if (iso == null || iso.isBlank()) {
            return "—";
        }
        try {
            return date(LocalDate.parse(iso.length() > 10 ? iso.substring(0, 10) : iso));
        } catch (Exception e) {
            return iso;
        }
    }

    public static String initiales(String nomComplet) {
        if (nomComplet == null || nomComplet.isBlank()) {
            return "RG";
        }
        String[] parties = nomComplet.trim().split("\\s+");
        String premiere = parties[0].substring(0, 1);
        String seconde = parties.length > 1 ? parties[parties.length - 1].substring(0, 1) : "";
        return (premiere + seconde).toUpperCase(Locale.FRANCE);
    }

    public static HBox entetePage(String icone, String titre, String sousTitre, Node... actions) {
        StackPane pastilleIcone = new StackPane(Icones.icone(icone));
        pastilleIcone.getStyleClass().add("icone-page");

        Label libelleTitre = new Label(titre);
        libelleTitre.getStyleClass().add("titre-page");
        Label libelleSousTitre = new Label(sousTitre);
        libelleSousTitre.getStyleClass().add("sous-titre");
        libelleSousTitre.setWrapText(true);
        VBox textes = new VBox(2, libelleTitre, libelleSousTitre);
        textes.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textes, Priority.ALWAYS);

        HBox zoneActions = new HBox(8, actions);
        zoneActions.setAlignment(Pos.CENTER_RIGHT);

        HBox entete = new HBox(14, pastilleIcone, textes, zoneActions);
        entete.setAlignment(Pos.CENTER_LEFT);
        entete.getStyleClass().add("entete-page");
        return entete;
    }

    public static Label pastille(String texte, String variante) {
        Label pastille = new Label(texte);
        pastille.getStyleClass().addAll("pastille", "pastille-" + variante);
        pastille.setMinWidth(Region.USE_PREF_SIZE);
        return pastille;
    }

    public static String libelleStatutSite(String code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case "PLANIFIE" -> "Planifié";
            case "EN_COURS" -> "En cours";
            case "TERMINE" -> "Terminé";
            case "SUSPENDU" -> "Suspendu";
            default -> code;
        };
    }

    public static String varianteStatutSite(String code) {
        if (code == null) {
            return "neutre";
        }
        return switch (code) {
            case "PLANIFIE" -> "planifie";
            case "EN_COURS" -> "canopee";
            case "TERMINE" -> "foret";
            case "SUSPENDU" -> "laterite";
            default -> "neutre";
        };
    }

    public static Label pastilleStatutSite(String code) {
        return pastille(libelleStatutSite(code), varianteStatutSite(code));
    }

    public static Color couleurSurvie(double taux) {
        if (taux >= 90) {
            return Color.web("#1F5136");
        }
        if (taux >= 80) {
            return Color.web("#3E8E5A");
        }
        if (taux >= 70) {
            return Color.web("#C99A1C");
        }
        if (taux >= 60) {
            return Color.web("#9A5B34");
        }
        return Color.web("#B3412E");
    }

    public static String hexSurvie(double taux) {
        Color c = couleurSurvie(taux);
        return String.format("#%02X%02X%02X", (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }

    public static HBox barreSurvie(Number taux, double largeurBarre) {
        Label valeur = new Label(taux == null ? "—" : pourcentage(taux));
        valeur.setPrefWidth(54);
        valeur.setMinWidth(Region.USE_PREF_SIZE);
        valeur.getStyleClass().add("texte-corps");
        if (taux == null) {
            HBox vide = new HBox(valeur);
            vide.setAlignment(Pos.CENTER_LEFT);
            return vide;
        }
        double t = Math.max(0, Math.min(100, taux.doubleValue()));
        Rectangle fond = new Rectangle(largeurBarre, 6);
        fond.setArcWidth(6);
        fond.setArcHeight(6);
        fond.setFill(Color.web("#E6EBE1"));
        Rectangle plein = new Rectangle(largeurBarre * t / 100.0, 6);
        plein.setArcWidth(6);
        plein.setArcHeight(6);
        plein.setFill(couleurSurvie(t));
        StackPane barre = new StackPane(fond, plein);
        barre.setAlignment(Pos.CENTER_LEFT);
        HBox ligne = new HBox(8, barre, valeur);
        ligne.setAlignment(Pos.CENTER_LEFT);
        return ligne;
    }

    public static HBox barreProgression(double fraction, double largeur, Color couleur) {
        double t = Math.max(0, Math.min(1, fraction));
        Rectangle fond = new Rectangle(largeur, 8);
        fond.setArcWidth(8);
        fond.setArcHeight(8);
        fond.setFill(Color.web("#E6EBE1"));
        Rectangle plein = new Rectangle(largeur * t, 8);
        plein.setArcWidth(8);
        plein.setArcHeight(8);
        plein.setFill(couleur);
        StackPane barre = new StackPane(fond, plein);
        barre.setAlignment(Pos.CENTER_LEFT);
        HBox conteneur = new HBox(barre);
        conteneur.setAlignment(Pos.CENTER_LEFT);
        return conteneur;
    }

    public record Champ(String cle, String valeur, boolean scientifique) {
        public Champ(String cle, String valeur) {
            this(cle, valeur, false);
        }
    }

    public static VBox etiquetteSpecimen(String titre, String reference, List<Champ> champs) {
        Label libelleTitre = new Label(titre.toUpperCase(Locale.FRANCE));
        Label libelleReference = new Label(reference);
        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);
        HBox entete = new HBox(libelleTitre, espace, libelleReference);
        entete.getStyleClass().add("entete-etiquette");

        VBox etiquette = new VBox(entete);
        etiquette.getStyleClass().add("etiquette-specimen");
        for (Champ champ : champs) {
            Label cle = new Label(champ.cle().toUpperCase(Locale.FRANCE));
            cle.getStyleClass().add("cle-etiquette");
            cle.setMinWidth(92);
            cle.setPrefWidth(92);
            Label valeur = new Label(champ.valeur() == null || champ.valeur().isBlank() ? "—" : champ.valeur());
            valeur.getStyleClass().add(champ.scientifique() ? "nom-scientifique" : "valeur-etiquette");
            valeur.setWrapText(true);
            HBox ligne = new HBox(8, cle, valeur);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.getStyleClass().add("ligne-etiquette");
            etiquette.getChildren().add(ligne);
        }
        return etiquette;
    }

    public static VBox etatVide(String titre, String texte) {
        Node dessin = Illustrations.feuille(Illustrations.Espece.OKOUME, 70, Color.web("#8FA894"), Color.web("#EEF3EA"));
        Label libelleTitre = new Label(titre);
        libelleTitre.getStyleClass().add("titre-3");
        Label libelleTexte = new Label(texte);
        libelleTexte.getStyleClass().add("texte-muet");
        libelleTexte.setWrapText(true);
        libelleTexte.setMaxWidth(360);
        libelleTexte.setMinHeight(Region.USE_PREF_SIZE);
        libelleTexte.setStyle("-fx-text-alignment: center;");
        VBox vide = new VBox(10, dessin, libelleTitre, libelleTexte);
        vide.setAlignment(Pos.CENTER);
        vide.getStyleClass().add("etat-vide");
        return vide;
    }

    public static VBox section(String titre, String sousTitre, Node contenu, Node... actions) {
        Label libelleTitre = new Label(titre);
        libelleTitre.getStyleClass().add("titre-section");
        VBox textes = new VBox(1, libelleTitre);
        if (sousTitre != null && !sousTitre.isBlank()) {
            Label libelleSousTitre = new Label(sousTitre);
            libelleSousTitre.getStyleClass().add("texte-muet");
            libelleSousTitre.setWrapText(true);
            textes.getChildren().add(libelleSousTitre);
        }
        HBox.setHgrow(textes, Priority.ALWAYS);
        HBox entete = new HBox(10, textes);
        entete.getChildren().addAll(actions);
        entete.setAlignment(Pos.CENTER_LEFT);
        entete.getStyleClass().add("feuille-titre");

        VBox corps = new VBox(contenu);
        corps.setPadding(new Insets(12, 16, 16, 16));
        VBox.setVgrow(corps, Priority.ALWAYS);
        VBox.setVgrow(contenu, Priority.ALWAYS);

        VBox feuille = new VBox(entete, corps);
        feuille.getStyleClass().add("feuille");
        return feuille;
    }

    public static Button bouton(String texte, String icone, String classe) {
        Button bouton = new Button(texte);
        if (icone != null) {
            bouton.setGraphic(Icones.icone(icone));
        }
        bouton.getStyleClass().add(classe);
        return bouton;
    }

    public static Button boutonExportPdf(String texte, Runnable action) {
        Button bouton = new Button(texte, Icones.icone(Icones.PDF));
        bouton.getGraphic().getStyleClass().add("icone-pdf");
        bouton.getStyleClass().add("bouton-export");
        bouton.setOnAction(e -> action.run());
        return bouton;
    }

    public static Button boutonExportExcel(String texte, Runnable action) {
        Button bouton = new Button(texte, Icones.icone(Icones.EXCEL));
        bouton.getGraphic().getStyleClass().add("icone-excel");
        bouton.getStyleClass().add("bouton-export");
        bouton.setOnAction(e -> action.run());
        return bouton;
    }

    public static Button boutonIcone(String icone, String infoBulle, String variante) {
        Button bouton = new Button();
        bouton.setGraphic(Icones.icone(icone));
        bouton.getStyleClass().add("bouton-icone");
        if (variante != null && !variante.isBlank()) {
            bouton.getStyleClass().add(variante);
        }
        bouton.setTooltip(new Tooltip(infoBulle));
        return bouton;
    }

    public static VBox indicateur(String icone, String valeur, String unite, String libelle) {
        Label libelleValeur = new Label(valeur);
        libelleValeur.getStyleClass().add("chiffre-fort");
        HBox ligneValeur = new HBox(5, libelleValeur);
        ligneValeur.setAlignment(Pos.BASELINE_LEFT);
        if (unite != null && !unite.isBlank()) {
            Label libelleUnite = new Label(unite);
            libelleUnite.getStyleClass().add("unite");
            ligneValeur.getChildren().add(libelleUnite);
        }
        Label libelleTexte = new Label(libelle);
        libelleTexte.getStyleClass().add("stat-label");
        HBox tete = new HBox(7, Icones.icone(icone), libelleTexte);
        tete.setAlignment(Pos.CENTER_LEFT);
        VBox bloc = new VBox(6, tete, ligneValeur);
        bloc.getStyleClass().addAll("feuille", "indicateur");
        return bloc;
    }

    public static Label chargement(String texte) {
        Label libelle = new Label(texte);
        libelle.getStyleClass().add("texte-muet");
        libelle.setPadding(new Insets(24));
        return libelle;
    }
}
