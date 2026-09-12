package com.reboisgabon.client.controllers;

import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.api.ApiException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.net.http.HttpResponse;

public class SplashController {

    @FXML
    private Label libelleStatut;

    @FXML
    private void verifierConnexion() {
        libelleStatut.getStyleClass().removeAll("badge-erreur", "badge-succes");
        libelleStatut.setText("Connexion en cours...");
        new Thread(() -> {
            try {
                HttpResponse<String> reponse = ApiClient.getInstance().get("health/");
                Platform.runLater(() -> {
                    libelleStatut.setText("API connectée — " + reponse.body());
                    libelleStatut.getStyleClass().add("badge-succes");
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    libelleStatut.setText("Erreur API — statut " + e.getStatutHttp());
                    libelleStatut.getStyleClass().add("badge-erreur");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    libelleStatut.setText("Impossible de joindre l'API : " + e.getMessage());
                    libelleStatut.getStyleClass().add("badge-erreur");
                });
            }
        }).start();
    }
}