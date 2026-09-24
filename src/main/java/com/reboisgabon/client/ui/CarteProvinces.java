package com.reboisgabon.client.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.util.JsonMapper;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class CarteProvinces extends Pane {

    private static final double ECHELLE = 100;
    private static JsonNode provincesGeojson;

    private final Group dessin = new Group();
    private final Group calqueProvinces = new Group();
    private final Group calqueSites = new Group();
    private final Group calqueEtiquettes = new Group();
    private final Map<String, SVGPath> formes = new HashMap<>();
    private final Map<String, double[]> centres = new HashMap<>();
    private double minLon = Double.MAX_VALUE;
    private double maxLon = -Double.MAX_VALUE;
    private double minLat = Double.MAX_VALUE;
    private double maxLat = -Double.MAX_VALUE;
    private Consumer<String> surClicProvince;

    public record Statistique(String province, int sites, double superficie, long plants, Double survie) {
    }

    public record Point(double latitude, double longitude, String statut, String nom) {
    }

    public CarteProvinces() {
        dessin.getChildren().addAll(calqueProvinces, calqueSites, calqueEtiquettes);
        getChildren().add(dessin);
        setMinSize(200, 200);
        construire();
    }

    public static JsonNode provinces() {
        if (provincesGeojson == null) {
            try (InputStream flux = CarteProvinces.class.getResourceAsStream("/com/reboisgabon/client/geo/provinces-gabon.geojson")) {
                provincesGeojson = JsonMapper.instance().readTree(flux);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return provincesGeojson;
    }

    public static String normaliser(String texte) {
        if (texte == null) {
            return "";
        }
        return Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private void construire() {
        JsonNode geo = provinces();
        for (JsonNode feature : geo.get("features")) {
            parcourirCoordonnees(feature.get("geometry").get("coordinates"), c -> {
                minLon = Math.min(minLon, c[0]);
                maxLon = Math.max(maxLon, c[0]);
                minLat = Math.min(minLat, c[1]);
                maxLat = Math.max(maxLat, c[1]);
            });
        }
        for (JsonNode feature : geo.get("features")) {
            String nom = feature.get("properties").get("nom").asText();
            StringBuilder chemin = new StringBuilder();
            List<double[]> points = new ArrayList<>();
            JsonNode geometrie = feature.get("geometry");
            List<JsonNode> polygones = new ArrayList<>();
            if ("MultiPolygon".equals(geometrie.get("type").asText())) {
                geometrie.get("coordinates").forEach(polygones::add);
            } else {
                polygones.add(geometrie.get("coordinates"));
            }
            double aireMax = -1;
            double[] centre = null;
            for (JsonNode polygone : polygones) {
                JsonNode anneau = polygone.get(0);
                double sx = 0;
                double sy = 0;
                int n = 0;
                for (int i = 0; i < anneau.size(); i++) {
                    double[] p = projeter(anneau.get(i).get(0).asDouble(), anneau.get(i).get(1).asDouble());
                    chemin.append(i == 0 ? "M" : " L").append(String.format(Locale.US, "%.2f,%.2f", p[0], p[1]));
                    sx += p[0];
                    sy += p[1];
                    n++;
                    points.add(p);
                }
                chemin.append(" Z ");
                double aire = anneau.size();
                if (aire > aireMax) {
                    aireMax = aire;
                    centre = new double[]{sx / n, sy / n};
                }
            }
            SVGPath forme = new SVGPath();
            forme.setContent(chemin.toString());
            forme.setFill(Color.web("#E4EBDD"));
            forme.setStroke(Color.WHITE);
            forme.setStrokeWidth(1.2);
            forme.setCursor(javafx.scene.Cursor.HAND);
            forme.setOnMouseEntered(e -> forme.setStroke(Color.web("#16291D")));
            forme.setOnMouseExited(e -> forme.setStroke(Color.WHITE));
            forme.setOnMouseClicked(e -> {
                if (surClicProvince != null) {
                    surClicProvince.accept(nom);
                }
            });
            calqueProvinces.getChildren().add(forme);
            formes.put(normaliser(nom), forme);
            centres.put(normaliser(nom), centre);

            Text etiquette = new Text(nom);
            etiquette.setFont(Font.font("Public Sans", FontWeight.BOLD, 9.5));
            etiquette.setFill(Color.web("#16291D"));
            etiquette.setOpacity(0.78);
            etiquette.setMouseTransparent(true);
            etiquette.setX(centre[0] - etiquette.getLayoutBounds().getWidth() / 2);
            etiquette.setY(centre[1]);
            calqueEtiquettes.getChildren().add(etiquette);
        }
    }

    private void parcourirCoordonnees(JsonNode noeud, Consumer<double[]> action) {
        if (noeud.isArray() && noeud.size() > 0 && noeud.get(0).isNumber()) {
            action.accept(new double[]{noeud.get(0).asDouble(), noeud.get(1).asDouble()});
            return;
        }
        for (JsonNode enfant : noeud) {
            parcourirCoordonnees(enfant, action);
        }
    }

    private double[] projeter(double lon, double lat) {
        return new double[]{(lon - minLon) * ECHELLE, (maxLat - lat) * ECHELLE};
    }

    public void definirSurClicProvince(Consumer<String> action) {
        this.surClicProvince = action;
    }

    public void appliquerStatistiques(List<Statistique> statistiques) {
        for (Statistique s : statistiques) {
            SVGPath forme = formes.get(normaliser(s.province()));
            if (forme == null) {
                continue;
            }
            if (s.survie() != null) {
                forme.setFill(Composants.couleurSurvie(s.survie()).deriveColor(0, 0.85, 1.12, 0.88));
            }
            Tooltip infobulle = new Tooltip(s.province()
                    + "\n" + s.sites() + " site" + (s.sites() > 1 ? "s" : "") + " · " + Composants.decimal(s.superficie()) + " ha"
                    + "\n" + Composants.nombre(s.plants()) + " plants"
                    + "\nSurvie moyenne : " + Composants.pourcentage(s.survie()));
            infobulle.setShowDelay(Duration.millis(120));
            Tooltip.install(forme, infobulle);
        }
        for (var noeud : calqueEtiquettes.getChildren()) {
            if (noeud instanceof Text t) {
                Statistique s = statistiques.stream().filter(x -> normaliser(x.province()).equals(normaliser(t.getText()))).findFirst().orElse(null);
                if (s != null && s.survie() != null && s.survie() < 80) {
                    t.setFill(Color.web("#16291D"));
                } else if (s != null && s.survie() != null) {
                    t.setFill(Color.WHITE);
                    t.setOpacity(0.92);
                }
            }
        }
    }

    public void afficherSites(List<Point> points) {
        calqueSites.getChildren().clear();
        for (Point p : points) {
            double[] xy = projeter(p.longitude(), p.latitude());
            Circle c = new Circle(xy[0], xy[1], 3.4);
            c.setFill(Color.WHITE);
            c.setStroke(Color.web("#16291D"));
            c.setStrokeWidth(1.4);
            Tooltip.install(c, new Tooltip(p.nom() + " — " + Composants.libelleStatutSite(p.statut())));
            calqueSites.getChildren().add(c);
        }
    }

    @Override
    protected void layoutChildren() {
        double largeurDessin = (maxLon - minLon) * ECHELLE;
        double hauteurDessin = (maxLat - minLat) * ECHELLE;
        double w = getWidth() - 12;
        double h = getHeight() - 12;
        if (w <= 0 || h <= 0) {
            return;
        }
        double k = Math.min(w / largeurDessin, h / hauteurDessin);
        dessin.getTransforms().setAll(new javafx.scene.transform.Scale(k, k, 0, 0));
        dessin.setLayoutX((getWidth() - largeurDessin * k) / 2);
        dessin.setLayoutY((getHeight() - hauteurDessin * k) / 2);
        for (var n : calqueSites.getChildren()) {
            if (n instanceof Circle c) {
                c.setRadius(3.4 / k);
                c.setStrokeWidth(1.3 / k);
            }
        }
        for (var n : calqueProvinces.getChildren()) {
            if (n instanceof SVGPath p) {
                p.setStrokeWidth(1.4 / k);
            }
        }
        for (var n : calqueEtiquettes.getChildren()) {
            if (n instanceof Text t) {
                t.setFont(Font.font("Public Sans", FontWeight.BOLD, 10.5 / k));
                double[] c = centres.get(normaliser(t.getText()));
                t.setX(c[0] - t.getLayoutBounds().getWidth() / 2);
                t.setY(c[1] - 9 / k);
            }
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        return 420;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 380;
    }
}
