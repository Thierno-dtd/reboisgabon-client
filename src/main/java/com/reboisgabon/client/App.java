package com.reboisgabon.client;

import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stagePrincipal) {
        SceneNavigator.getInstance().initialiser(stagePrincipal);
        ApiClient.getInstance().definirEcouteurExpiration(() ->
                SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/splash.fxml"));
        stagePrincipal.setTitle("ReboisGabon");
        stagePrincipal.setMinWidth(1200);
        stagePrincipal.setMinHeight(760);
        SceneNavigator.getInstance().naviguerVers("/com/reboisgabon/client/fxml/splash.fxml");
        stagePrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}