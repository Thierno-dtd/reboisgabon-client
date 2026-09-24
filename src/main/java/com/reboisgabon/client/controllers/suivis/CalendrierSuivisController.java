package com.reboisgabon.client.controllers.suivis;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.endpoints.CalendrierApi;
import com.reboisgabon.client.ui.Composants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.ResourceBundle;

public class CalendrierSuivisController implements Initializable {

    @FXML private ComboBox<Integer> comboHorizon;
    @FXML private VBox colonneRetard;
    @FXML private VBox colonneAVenir;

    private final CalendrierApi calendrierApi = new CalendrierApi();
    private final VBox listeRetard = new VBox();
    private final VBox listeAVenir = new VBox();
    private int pageRetard = 1;
    private int pageAVenir = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboHorizon.getItems().addAll(7, 15, 30, 60, 90);
        comboHorizon.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer jours) {
                return jours == null ? "" : jours + " prochains jours";
            }

            @Override
            public Integer fromString(String texte) {
                return null;
            }
        });
        comboHorizon.setValue(30);
        comboHorizon.valueProperty().addListener((o, a, n) -> actualiser());
        actualiser();
    }

    @FXML
    private void actualiser() {
        pageRetard = 1;
        pageAVenir = 1;
        listeRetard.getChildren().clear();
        listeAVenir.getChildren().clear();
        charger(true, true);
    }

    private void charger(boolean retard, boolean aVenir) {
        int horizon = comboHorizon.getValue() == null ? 30 : comboHorizon.getValue();
        new Thread(() -> {
            try {
                JsonNode resultat = calendrierApi.charger(horizon, pageAVenir, pageRetard);
                Platform.runLater(() -> {
                    if (retard) {
                        remplir(colonneRetard, listeRetard, resultat.path("suivis_en_retard"), true);
                    }
                    if (aVenir) {
                        remplir(colonneAVenir, listeAVenir, resultat.path("suivis_a_venir"), false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label message = new Label("Le calendrier n'a pas pu être chargé.");
                    message.getStyleClass().add("message-erreur");
                    colonneAVenir.getChildren().setAll(message);
                });
            }
        }).start();
    }

    private void remplir(VBox colonne, VBox liste, JsonNode bloc, boolean enRetard) {
        int total = bloc.path("count").asInt();
        for (JsonNode s : bloc.path("results")) {
            liste.getChildren().add(ligne(s, enRetard));
        }
        Label titre = new Label(enRetard ? "Contrôles en retard" : "Contrôles à venir");
        titre.getStyleClass().add("titre-section");
        Label sous = new Label(enRetard ? "La date prévue est dépassée : à programmer en priorité." : "Passages prévus sur la période choisie.");
        sous.getStyleClass().add("texte-muet");
        VBox textes = new VBox(2, titre, sous);
        HBox.setHgrow(textes, Priority.ALWAYS);
        HBox entete = new HBox(10, textes, Composants.pastille(String.valueOf(total), enRetard ? (total > 0 ? "laterite" : "foret") : "ocean"));
        entete.setAlignment(Pos.CENTER_LEFT);
        entete.getStyleClass().add("feuille-titre");

        VBox corps = new VBox(liste);
        corps.setPadding(new Insets(4, 16, 12, 16));
        if (liste.getChildren().isEmpty()) {
            corps.getChildren().setAll(Composants.etatVide(enRetard ? "Aucun retard" : "Rien de prévu",
                    enRetard ? "Tous les contrôles ont été réalisés à temps." : "Aucun contrôle n'est programmé sur cette période."));
        }
        if (bloc.path("has_next").asBoolean()) {
            Button plus = Composants.bouton("Afficher plus (" + (total - liste.getChildren().size()) + " restants)", null, "bouton-lien");
            plus.setOnAction(e -> {
                if (enRetard) {
                    pageRetard++;
                } else {
                    pageAVenir++;
                }
                charger(enRetard, !enRetard);
            });
            corps.getChildren().add(plus);
        }
        colonne.getChildren().setAll(entete, corps);
    }

    private HBox ligne(JsonNode s, boolean enRetard) {
        LocalDate date = LocalDate.parse(s.path("prochaine_date_controle").asText());
        Label jour = new Label(String.valueOf(date.getDayOfMonth()));
        jour.getStyleClass().add("chiffre-moyen");
        jour.setStyle("-fx-text-fill: " + (enRetard ? "#9A5B34" : "#1F5136") + ";");
        Label mois = new Label(date.format(DateTimeFormatter.ofPattern("MMM yy", Locale.FRANCE)).toUpperCase(Locale.FRANCE));
        mois.getStyleClass().add("cle-etiquette");
        VBox blocDate = new VBox(-2, jour, mois);
        blocDate.setAlignment(Pos.CENTER);
        blocDate.setMinWidth(54);
        blocDate.setPadding(new Insets(4));
        blocDate.setStyle("-fx-background-color: " + (enRetard ? "#F4E6DB" : "#E4EFDE") + "; -fx-background-radius: 6;");

        Label site = new Label(s.path("site").asText());
        site.getStyleClass().add("titre-3");
        long ecart = ChronoUnit.DAYS.between(LocalDate.now(), date);
        String quand = ecart == 0 ? "aujourd'hui" : ecart > 0 ? "dans " + ecart + " j" : "en retard de " + (-ecart) + " j";
        Label detail = new Label(s.path("essence").asText() + "  ·  " + quand);
        detail.getStyleClass().add("texte-petit");
        VBox textes = new VBox(1, site, detail);
        HBox.setHgrow(textes, Priority.ALWAYS);
        textes.setMinWidth(0);
        Double taux = s.hasNonNull("dernier_taux_survie") ? s.get("dernier_taux_survie").asDouble() : null;
        HBox ligne = new HBox(12, blocDate, textes, Composants.barreSurvie(taux, 50));
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.getStyleClass().add("ligne-liste");
        ligne.setPadding(new Insets(8, 2, 8, 2));
        return ligne;
    }
}
