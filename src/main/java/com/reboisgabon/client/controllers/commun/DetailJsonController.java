package com.reboisgabon.client.controllers.commun;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.util.JsonVueUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DetailJsonController {

    @FXML private Label libelleTitre;
    @FXML private VBox conteneurContenu;

    public void definirTitre(String titre) {
        libelleTitre.setText(titre);
    }

    public void definirContenu(JsonNode contenu) {
        conteneurContenu.getChildren().setAll(JsonVueUtil.construireVue(contenu));
    }

    @FXML
    private void fermer() {
        ((Stage) libelleTitre.getScene().getWindow()).close();
    }
}