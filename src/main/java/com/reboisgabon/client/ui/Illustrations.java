package com.reboisgabon.client.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.Locale;
import java.util.Random;

public final class Illustrations {

    public static final Color ENCRE = Color.web("#16291D");
    public static final Color FEUILLE = Color.web("#E4EFDE");
    public static final Color FORET = Color.web("#1F5136");
    public static final Color OR = Color.web("#C99A1C");

    private Illustrations() {
    }

    public enum Espece {
        OKOUME, MOABI, PADOUK, KEVAZINGO
    }

    public static Node feuille(Espece espece, double hauteur, Color trait, Color remplissage) {
        Group dessin = switch (espece) {
            case OKOUME -> okoume(trait, remplissage);
            case MOABI -> moabi(trait, remplissage);
            case PADOUK -> padouk(trait, remplissage);
            case KEVAZINGO -> kevazingo(trait, remplissage);
        };
        double echelle = hauteur / 220.0;
        dessin.setScaleX(echelle);
        dessin.setScaleY(echelle);
        Group conteneur = new Group(dessin);
        conteneur.setMouseTransparent(true);
        return conteneur;
    }

    public static Espece especePour(String nomEssence) {
        if (nomEssence == null) {
            return Espece.OKOUME;
        }
        String n = nomEssence.toLowerCase(Locale.ROOT);
        if (n.contains("moabi") || n.contains("azob") || n.contains("ozigo")) {
            return Espece.MOABI;
        }
        if (n.contains("padouk") || n.contains("tali") || n.contains("acacia")) {
            return Espece.PADOUK;
        }
        if (n.contains("kevazingo") || n.contains("movingui") || n.contains("eucalyptus")) {
            return Espece.KEVAZINGO;
        }
        return Espece.OKOUME;
    }

    private static String f(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private static double[] tourner(double x, double y, double angle, double ox, double oy) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new double[]{ox + x * c - y * s, oy + x * s + y * c};
    }

    private static String foliole(double ox, double oy, double longueur, double largeur, double angle, double pointe) {
        double[][] p = {
                {0, 0},
                {longueur * 0.22, -largeur}, {longueur * (0.72 - pointe * 0.1), -largeur * (0.95 - pointe * 0.3)}, {longueur, 0},
                {longueur * (0.72 - pointe * 0.1), largeur * (0.95 - pointe * 0.3)}, {longueur * 0.22, largeur}, {0, 0}
        };
        StringBuilder sb = new StringBuilder();
        double[] a = tourner(p[0][0], p[0][1], angle, ox, oy);
        sb.append("M").append(f(a[0])).append(",").append(f(a[1]));
        for (int i = 1; i < p.length; i += 3) {
            double[] c1 = tourner(p[i][0], p[i][1], angle, ox, oy);
            double[] c2 = tourner(p[i + 1][0], p[i + 1][1], angle, ox, oy);
            double[] e = tourner(p[i + 2][0], p[i + 2][1], angle, ox, oy);
            sb.append(" C").append(f(c1[0])).append(",").append(f(c1[1])).append(" ")
                    .append(f(c2[0])).append(",").append(f(c2[1])).append(" ")
                    .append(f(e[0])).append(",").append(f(e[1]));
        }
        return sb.append(" Z").toString();
    }

    private static String nervure(double ox, double oy, double longueur, double angle) {
        double[] a = tourner(longueur * 0.04, 0, angle, ox, oy);
        double[] b = tourner(longueur * 0.86, 0, angle, ox, oy);
        return "M" + f(a[0]) + "," + f(a[1]) + " L" + f(b[0]) + "," + f(b[1]);
    }

    private static SVGPath forme(String contenu, Color trait, Color remplissage, double epaisseur) {
        SVGPath chemin = new SVGPath();
        chemin.setContent(contenu);
        chemin.setStroke(trait);
        chemin.setStrokeWidth(epaisseur);
        chemin.setFill(remplissage);
        chemin.setStrokeLineCap(StrokeLineCap.ROUND);
        chemin.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return chemin;
    }

    private static Group okoume(Color trait, Color remplissage) {
        Group g = new Group();
        double baseX = 60;
        double baseY = 220;
        double sommetY = 22;
        g.getChildren().add(forme("M" + f(baseX) + "," + f(baseY) + " C" + f(baseX + 4) + ",150 " + f(baseX - 3) + ",90 " + f(baseX + 2) + "," + f(sommetY + 26), trait, null, 1.6));
        StringBuilder nervures = new StringBuilder();
        int paires = 5;
        for (int i = 0; i < paires; i++) {
            double t = i / (double) paires;
            double y = 188 - t * 150;
            double x = baseX + Math.sin(t * 3) * 2;
            double longueur = 62 - t * 16;
            double largeur = 11 - t * 2;
            double angleGauche = Math.toRadians(-150 + t * 12);
            double angleDroit = Math.toRadians(-30 - t * 12);
            g.getChildren().add(forme(foliole(x, y, longueur, largeur, angleGauche, 0.4), trait, remplissage, 1.2));
            g.getChildren().add(forme(foliole(x, y - 4, longueur, largeur, angleDroit, 0.4), trait, remplissage, 1.2));
            nervures.append(nervure(x, y, longueur, angleGauche)).append(" ");
            nervures.append(nervure(x, y - 4, longueur, angleDroit)).append(" ");
        }
        g.getChildren().add(forme(foliole(baseX + 2, sommetY + 30, 44, 9, Math.toRadians(-92), 0.5), trait, remplissage, 1.2));
        nervures.append(nervure(baseX + 2, sommetY + 30, 44, Math.toRadians(-92)));
        g.getChildren().add(forme(nervures.toString(), trait, null, 0.7));
        return g;
    }

    private static Group moabi(Color trait, Color remplissage) {
        Group g = new Group();
        String contour = "M70,214 C68,196 66,186 62,176 C30,150 14,110 18,72 C22,38 44,14 70,8 C96,14 118,38 122,72 C126,110 110,150 78,176 C74,186 72,196 70,214 Z";
        g.getChildren().add(forme(contour, trait, remplissage, 1.4));
        StringBuilder v = new StringBuilder("M70,210 C70,150 70,80 70,14 ");
        for (int i = 0; i < 9; i++) {
            double y = 168 - i * 17;
            double portee = 34 + Math.sin(i / 8.0 * Math.PI) * 18;
            v.append("M70,").append(f(y)).append(" C").append(f(70 - portee * 0.4)).append(",").append(f(y - 6))
                    .append(" ").append(f(70 - portee * 0.8)).append(",").append(f(y - 14)).append(" ")
                    .append(f(70 - portee)).append(",").append(f(y - 22)).append(" ");
            v.append("M70,").append(f(y)).append(" C").append(f(70 + portee * 0.4)).append(",").append(f(y - 6))
                    .append(" ").append(f(70 + portee * 0.8)).append(",").append(f(y - 14)).append(" ")
                    .append(f(70 + portee)).append(",").append(f(y - 22)).append(" ");
        }
        g.getChildren().add(forme(v.toString(), trait, null, 0.75));
        return g;
    }

    private static Group padouk(Color trait, Color remplissage) {
        Group g = new Group();
        g.getChildren().add(forme("M48,220 C50,170 46,120 52,60", trait, null, 1.5));
        StringBuilder nervures = new StringBuilder();
        double[][] folioles = {
                {49, 178, -158, 50, 16}, {50, 150, -24, 52, 16}, {49, 118, -152, 48, 15}, {51, 92, -30, 46, 14}, {52, 62, -86, 44, 14}
        };
        for (double[] fo : folioles) {
            double angle = Math.toRadians(fo[2]);
            g.getChildren().add(forme(foliole(fo[0], fo[1], fo[3], fo[4], angle, 0.9), trait, remplissage, 1.2));
            nervures.append(nervure(fo[0], fo[1], fo[3], angle)).append(" ");
        }
        g.getChildren().add(forme(nervures.toString(), trait, null, 0.7));
        Circle aile = new Circle(118, 190, 24);
        aile.setFill(remplissage == null ? null : remplissage.deriveColor(0, 1, 0.97, 1));
        aile.setStroke(trait);
        aile.setStrokeWidth(1.2);
        SVGPath graine = forme("M111,190 C111,183 125,183 125,190 C125,197 111,197 111,190 Z M118,166 L118,183 M118,197 L118,214 M94,190 L111,190 M125,190 L142,190 M101,173 L113,185 M123,195 L135,207 M135,173 L123,185 M113,195 L101,207", trait, null, 0.7);
        g.getChildren().addAll(aile, graine, forme("M96,196 C88,204 80,210 70,214", trait, null, 1.0));
        return g;
    }

    private static Group kevazingo(Color trait, Color remplissage) {
        Group g = new Group();
        g.getChildren().add(forme("M70,220 C70,200 71,184 72,170", trait, null, 1.6));
        String gauche = "M72,170 C40,160 18,120 22,70 C26,40 40,22 52,16 C60,60 70,120 72,170 Z";
        String droite = "M72,170 C104,160 126,120 122,70 C118,40 104,22 92,16 C84,60 74,120 72,170 Z";
        g.getChildren().add(forme(gauche, trait, remplissage, 1.3));
        g.getChildren().add(forme(droite, trait, remplissage, 1.3));
        StringBuilder v = new StringBuilder("M71,166 C62,120 56,60 52,20 M73,166 C82,120 88,60 92,20 ");
        for (int i = 0; i < 6; i++) {
            double y = 150 - i * 22;
            v.append("M").append(f(64 - i * 1.6)).append(",").append(f(y)).append(" L").append(f(36 - i * 0.5)).append(",").append(f(y - 18)).append(" ");
            v.append("M").append(f(80 + i * 1.6)).append(",").append(f(y)).append(" L").append(f(108 + i * 0.5)).append(",").append(f(y - 18)).append(" ");
        }
        g.getChildren().add(forme(v.toString(), trait, null, 0.7));
        return g;
    }

    public static Node logo(double taille) {
        StackPane conteneur = new StackPane();
        conteneur.setMinSize(taille, taille);
        conteneur.setMaxSize(taille, taille);
        Rectangle fond = new Rectangle(taille, taille);
        fond.setArcWidth(taille * 0.34);
        fond.setArcHeight(taille * 0.34);
        fond.setFill(OR);
        double e = taille / 40.0;
        SVGPath marque = new SVGPath();
        marque.setContent("M20,34 L20,15 M20,24 C14,22 9,17 8,9 C15,9 19,14 20,20 M20,20 C21,13 26,7 33,6 C33,14 28,19 20,21 M20,15 C18,11 18,7 20,3 C22,7 22,11 20,15");
        marque.setStroke(Color.web("#12281B"));
        marque.setStrokeWidth(2.4);
        marque.setFill(null);
        marque.setStrokeLineCap(StrokeLineCap.ROUND);
        marque.setStrokeLineJoin(StrokeLineJoin.ROUND);
        marque.setScaleX(e);
        marque.setScaleY(e);
        conteneur.getChildren().addAll(fond, new Group(marque));
        return conteneur;
    }

    public static class CourbesNiveau extends Pane {

        private final Color couleur;
        private final double opacite;
        private final long graine;
        private double derniereLargeur = -1;
        private double derniereHauteur = -1;

        public CourbesNiveau() {
            this(Color.WHITE, 0.09, 7);
        }

        public CourbesNiveau(Color couleur, double opacite, long graine) {
            this.couleur = couleur;
            this.opacite = opacite;
            this.graine = graine;
            setMouseTransparent(true);
            setMinSize(0, 0);
            setPrefSize(10, 10);
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(widthProperty());
            clip.heightProperty().bind(heightProperty());
            setClip(clip);
        }

        @Override
        protected void layoutChildren() {
            double l = getWidth();
            double h = getHeight();
            if (l <= 0 || h <= 0 || (Math.abs(l - derniereLargeur) < 1 && Math.abs(h - derniereHauteur) < 1)) {
                return;
            }
            derniereLargeur = l;
            derniereHauteur = h;
            getChildren().clear();
            Random aleatoire = new Random(graine);
            double[][] centres = {
                    {0.82, 0.30, 1.0}, {0.30, 1.05, 0.9}, {1.05, 1.1, 0.7}
            };
            double base = Math.max(l, h);
            StringBuilder sb = new StringBuilder();
            for (double[] c : centres) {
                double cx = c[0] * l;
                double cy = c[1] * h;
                double phase1 = aleatoire.nextDouble() * Math.PI * 2;
                double phase2 = aleatoire.nextDouble() * Math.PI * 2;
                int niveaux = 11;
                for (int n = 1; n <= niveaux; n++) {
                    double r = base * c[2] * n / (niveaux * 1.6);
                    sb.append(boucle(cx, cy, r, phase1 + n * 0.12, phase2 - n * 0.07));
                }
            }
            SVGPath lignes = new SVGPath();
            lignes.setContent(sb.toString());
            lignes.setFill(null);
            lignes.setStroke(couleur);
            lignes.setOpacity(opacite);
            lignes.setStrokeWidth(1);
            getChildren().add(lignes);
        }

        private String boucle(double cx, double cy, double r, double p1, double p2) {
            int n = 36;
            double[][] pts = new double[n][2];
            for (int i = 0; i < n; i++) {
                double a = i * Math.PI * 2 / n;
                double rr = r * (1 + 0.16 * Math.sin(3 * a + p1) + 0.07 * Math.sin(5 * a + p2));
                pts[i][0] = cx + rr * Math.cos(a) * 1.25;
                pts[i][1] = cy + rr * Math.sin(a) * 0.85;
            }
            StringBuilder sb = new StringBuilder("M").append(f(pts[0][0])).append(",").append(f(pts[0][1]));
            for (int i = 0; i < n; i++) {
                double[] p0 = pts[(i - 1 + n) % n];
                double[] p1p = pts[i];
                double[] p2p = pts[(i + 1) % n];
                double[] p3 = pts[(i + 2) % n];
                sb.append(" C").append(f(p1p[0] + (p2p[0] - p0[0]) / 6)).append(",").append(f(p1p[1] + (p2p[1] - p0[1]) / 6))
                        .append(" ").append(f(p2p[0] - (p3[0] - p1p[0]) / 6)).append(",").append(f(p2p[1] - (p3[1] - p1p[1]) / 6))
                        .append(" ").append(f(p2p[0])).append(",").append(f(p2p[1]));
            }
            return sb.append(" Z ").toString();
        }
    }

    public static class Canopee extends Pane {

        private final Color couleur;
        private final double hauteurMax;
        private final long graine;
        private double derniereLargeur = -1;

        public Canopee() {
            this(Color.web("#0D1F15"), 90, 3);
        }

        public Canopee(Color couleur, double hauteurMax, long graine) {
            this.couleur = couleur;
            this.hauteurMax = hauteurMax;
            this.graine = graine;
            setMouseTransparent(true);
            setMinHeight(hauteurMax);
            setPrefHeight(hauteurMax);
            setMaxHeight(hauteurMax);
            setMinWidth(0);
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(widthProperty());
            clip.heightProperty().bind(heightProperty());
            setClip(clip);
        }

        @Override
        protected void layoutChildren() {
            double l = getWidth();
            if (l <= 0 || Math.abs(l - derniereLargeur) < 1) {
                return;
            }
            derniereLargeur = l;
            getChildren().clear();
            Random aleatoire = new Random(graine);
            double h = hauteurMax;
            StringBuilder sb = new StringBuilder("M0," + f(h));
            double x = 0;
            double sol = h * 0.62;
            sb.append(" L0,").append(f(sol));
            while (x < l) {
                boolean emergent = aleatoire.nextDouble() < 0.18;
                double largeur = emergent ? 46 + aleatoire.nextDouble() * 30 : 22 + aleatoire.nextDouble() * 26;
                double sommet = emergent ? h * (0.02 + aleatoire.nextDouble() * 0.12) : h * (0.30 + aleatoire.nextDouble() * 0.22);
                if (emergent) {
                    double tronc = sol - 4;
                    double cx = x + largeur / 2;
                    sb.append(" L").append(f(cx - 2)).append(",").append(f(tronc));
                    sb.append(" L").append(f(cx - 2)).append(",").append(f(sommet + 16));
                    sb.append(" C").append(f(cx - largeur * 0.7)).append(",").append(f(sommet + 18)).append(" ")
                            .append(f(cx - largeur * 0.55)).append(",").append(f(sommet - 2)).append(" ")
                            .append(f(cx - largeur * 0.1)).append(",").append(f(sommet));
                    sb.append(" C").append(f(cx + largeur * 0.3)).append(",").append(f(sommet - 6)).append(" ")
                            .append(f(cx + largeur * 0.75)).append(",").append(f(sommet + 4)).append(" ")
                            .append(f(cx + 2)).append(",").append(f(sommet + 16));
                    sb.append(" L").append(f(cx + 2)).append(",").append(f(tronc));
                    sb.append(" L").append(f(x + largeur)).append(",").append(f(sol));
                } else {
                    sb.append(" C").append(f(x + largeur * 0.05)).append(",").append(f(sommet)).append(" ")
                            .append(f(x + largeur * 0.95)).append(",").append(f(sommet)).append(" ")
                            .append(f(x + largeur)).append(",").append(f(sol - aleatoire.nextDouble() * 8));
                }
                x += largeur * (emergent ? 1 : 0.8);
                if (x > l) {
                    sb.append(" L").append(f(l)).append(",").append(f(sol));
                }
            }
            sb.append(" L").append(f(l)).append(",").append(f(h)).append(" Z");
            SVGPath silhouette = new SVGPath();
            silhouette.setContent(sb.toString());
            silhouette.setFill(couleur);
            getChildren().add(silhouette);
        }
    }

    public static Node hachures(double largeur, double hauteur, Color couleur) {
        StringBuilder sb = new StringBuilder();
        for (double x = -hauteur; x < largeur; x += 6) {
            sb.append("M").append(f(x)).append(",").append(f(hauteur)).append(" L").append(f(x + hauteur)).append(",0 ");
        }
        SVGPath p = new SVGPath();
        p.setContent(sb.toString());
        p.setStroke(couleur);
        p.setStrokeWidth(1);
        Rectangle clip = new Rectangle(largeur, hauteur);
        p.setClip(clip);
        return p;
    }
}
