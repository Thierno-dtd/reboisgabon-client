package com.reboisgabon.client;

import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.ui.Polices;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stagePrincipal) {
        Polices.charger();
        SceneNavigator.getInstance().initialiser(stagePrincipal);
        ApiClient.getInstance().definirEcouteurExpiration(() ->
                SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/login.fxml"));
        stagePrincipal.setTitle("ReboisGabon — Pilotage du reboisement");
        stagePrincipal.setMinWidth(1180);
        stagePrincipal.setMinHeight(720);
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/login.fxml");
        stagePrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}