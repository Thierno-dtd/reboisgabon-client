package com.reboisgabon.client.controllers.suivis;

import com.reboisgabon.client.api.endpoints.CalendrierApi;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.JsonVueUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class CalendrierSuivisController implements Initializable {

    @FXML private TextField champHorizon;
    @FXML private VBox conteneurResultats;

    private final CalendrierApi calendrierApi = new CalendrierApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actualiser();
    }

    @FXML
    private void actualiser() {
        int horizon;
        try {
            horizon = Integer.parseInt(champHorizon.getText());
        } catch (NumberFormatException e) {
            horizon = 30;
        }
        int horizonFinal = horizon;
        new Thread(() -> {
            try {
                var resultat = calendrierApi.charger(horizonFinal, 1, 1);
                Platform.runLater(() -> {
                    conteneurResultats.getChildren().clear();
                    Label titreAVenir = new Label("Suivis à venir");
                    titreAVenir.getStyleClass().add("titre-2");
                    conteneurResultats.getChildren().add(titreAVenir);
                    conteneurResultats.getChildren().add(JsonVueUtil.construireBlocPagine(
                            resultat.path("suivis_a_venir"),
                            page -> calendrierApi.charger(horizonFinal, page, 1).path("suivis_a_venir")));
                    Label titreEnRetard = new Label("Suivis en retard");
                    titreEnRetard.getStyleClass().add("titre-2");
                    conteneurResultats.getChildren().add(titreEnRetard);
                    conteneurResultats.getChildren().add(JsonVueUtil.construireBlocPagine(
                            resultat.path("suivis_en_retard"),
                            page -> calendrierApi.charger(horizonFinal, 1, page).path("suivis_en_retard")));
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger le calendrier."));
            }
        }).start();
    }
}