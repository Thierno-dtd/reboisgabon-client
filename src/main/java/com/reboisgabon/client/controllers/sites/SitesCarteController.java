package com.reboisgabon.client.controllers.sites;

import com.reboisgabon.client.api.endpoints.GeolocalisationApi;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.JsonVueUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class SitesCarteController implements Initializable {

    @FXML private WebView carte;
    @FXML private TextField champLatitude;
    @FXML private TextField champLongitude;
    @FXML private TextField champRayon;
    @FXML private VBox conteneurResultatsProximite;

    private final GeolocalisationApi geolocalisationApi = new GeolocalisationApi();
    private double derniereLatitude;
    private double derniereLongitude;
    private double dernierRayon;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerCarte();
    }

    private void chargerCarte() {
        new Thread(() -> {
            try {
                String geojson = geolocalisationApi.sitesGeojson();
                Platform.runLater(() -> carte.getEngine().loadContent(construireHtmlCarte(geojson), "text/html"));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger la carte des sites."));
            }
        }).start();
    }

    private String construireHtmlCarte(String geojson) {
        return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/>"
                + "<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\"/>"
                + "<style>html,body,#carte{height:100%;margin:0;padding:0;background:#0B140F;}</style>"
                + "</head><body><div id=\"carte\"></div>"
                + "<script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>"
                + "<script>"
                + "var carte = L.map('carte').setView([-0.8, 11.6], 6);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(carte);"
                + "var donnees = " + geojson + ";"
                + "var couche = L.geoJSON(donnees, {onEachFeature: function(feature, calque) {"
                + "  if (feature.properties && feature.properties.nom) { calque.bindPopup(feature.properties.nom); }"
                + "}}).addTo(carte);"
                + "if (donnees.features && donnees.features.length > 0) { carte.fitBounds(couche.getBounds()); }"
                + "</script></body></html>";
    }

    @FXML
    private void rechercherProximite() {
        try {
            derniereLatitude = Double.parseDouble(champLatitude.getText());
            derniereLongitude = Double.parseDouble(champLongitude.getText());
            dernierRayon = Double.parseDouble(champRayon.getText());
        } catch (NumberFormatException e) {
            AlertUtil.erreur("Erreur", "Latitude, longitude et rayon doivent être des nombres valides.");
            return;
        }
        new Thread(() -> {
            try {
                var resultat = geolocalisationApi.sitesProximite(derniereLatitude, derniereLongitude, dernierRayon, 1);
                Platform.runLater(() -> conteneurResultatsProximite.getChildren().setAll(
                        JsonVueUtil.construireBlocPagine(resultat, page ->
                                geolocalisationApi.sitesProximite(derniereLatitude, derniereLongitude, dernierRayon, page))));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les sites à proximité."));
            }
        }).start();
    }
}