package com.reboisgabon.client.controllers.intelligence;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.IntelligenceApi;
import com.reboisgabon.client.dto.essences.Essence;
import com.reboisgabon.client.dto.intelligence.PredictionSurvieReponse;
import com.reboisgabon.client.dto.intelligence.PredictionSurvieRequete;
import com.reboisgabon.client.session.Role;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.EssenceCache;
import com.reboisgabon.client.util.JsonVueUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class IntelligenceController implements Initializable {

    @FXML private Button boutonReentrainer;
    @FXML private ComboBox<Essence> comboEssence;
    @FXML private TextField champProvince;
    @FXML private TextField champSuperficie;
    @FXML private TextField champNombrePlants;
    @FXML private TextField champMoisPlantation;
    @FXML private CheckBox caseCroissanceRapide;
    @FXML private Button boutonPredire;
    @FXML private Label libelleErreurPrediction;
    @FXML private VBox conteneurResultatPrediction;
    @FXML private TextField champProvinceRecommandation;
    @FXML private TextField champTopN;
    @FXML private VBox conteneurRecommandation;
    @FXML private VBox conteneurDetectionRisque;

    private final IntelligenceApi intelligenceApi = new IntelligenceApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        EssenceCache.getInstance().assurerCharge(essences -> comboEssence.getItems().setAll(essences));
        Role role = SessionManager.getInstance().getRole();
        boolean peutReentrainer = role == Role.ADMIN || role == Role.SUPERVISEUR;
        boutonReentrainer.setVisible(peutReentrainer);
        boutonReentrainer.setManaged(peutReentrainer);
        chargerDetectionRisque(1);
    }

    @FXML
    private void predire() {
        if (comboEssence.getValue() == null || champProvince.getText() == null || champProvince.getText().isBlank()) {
            afficherErreurPrediction("Essence et province sont obligatoires.");
            return;
        }
        PredictionSurvieRequete requete = new PredictionSurvieRequete();
        requete.setEssence(comboEssence.getValue().getNom());
        requete.setProvince(champProvince.getText());
        requete.setCroissanceRapide(caseCroissanceRapide.isSelected());
        try {
            requete.setSuperficieSite(new BigDecimal(champSuperficie.getText()));
            requete.setNombrePlants(Integer.parseInt(champNombrePlants.getText()));
            requete.setMoisPlantation(Integer.parseInt(champMoisPlantation.getText()));
        } catch (NumberFormatException e) {
            afficherErreurPrediction("Superficie, nombre de plants et mois doivent être des nombres valides.");
            return;
        }
        masquerErreurPrediction();
        boutonPredire.setDisable(true);
        new Thread(() -> {
            try {
                PredictionSurvieReponse reponse = intelligenceApi.predireSurvie(requete);
                Platform.runLater(() -> {
                    boutonPredire.setDisable(false);
                    afficherResultatPrediction(reponse);
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonPredire.setDisable(false);
                    if (e.getStatutHttp() == 503) {
                        afficherErreurPrediction("Le modèle de prédiction n'est pas encore entraîné côté serveur.");
                    } else {
                        afficherErreurPrediction("Impossible de calculer la prédiction.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonPredire.setDisable(false);
                    afficherErreurPrediction("Impossible de joindre le serveur.");
                });
            }
        }).start();
    }

    private void afficherResultatPrediction(PredictionSurvieReponse reponse) {
        conteneurResultatPrediction.getChildren().clear();
        Label libelleTaux = new Label("Taux de survie prédit : " + reponse.getTauxSurviePredit() + " %");
        libelleTaux.getStyleClass().add("titre-2");
        Label libelleConfiance = new Label("Niveau de confiance : " + reponse.getNiveauConfiance());
        libelleConfiance.getStyleClass().add("texte-corps");
        Label libelleRisque = new Label(reponse.isCampagneARisque() ? "Campagne à risque" : "Campagne non à risque");
        libelleRisque.getStyleClass().add(reponse.isCampagneARisque() ? "badge-erreur" : "badge-succes");
        conteneurResultatPrediction.getChildren().addAll(libelleTaux, libelleConfiance, libelleRisque);
    }

    @FXML
    private void recommander() {
        String province = champProvinceRecommandation.getText();
        int topN;
        try {
            topN = Integer.parseInt(champTopN.getText());
        } catch (NumberFormatException e) {
            topN = 3;
        }
        int topNFinal = topN;
        new Thread(() -> {
            try {
                var resultat = intelligenceApi.recommanderEssence(province == null ? "" : province, topNFinal);
                Platform.runLater(() -> conteneurRecommandation.getChildren().setAll(JsonVueUtil.construireVue(resultat)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les recommandations."));
            }
        }).start();
    }

    private void chargerDetectionRisque(int page) {
        new Thread(() -> {
            try {
                var resultat = intelligenceApi.detectionRisque(page);
                Platform.runLater(() -> conteneurDetectionRisque.getChildren().setAll(
                        JsonVueUtil.construireBlocPagine(resultat, intelligenceApi::detectionRisque)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger la détection de risques."));
            }
        }).start();
    }

    @FXML
    private void reentrainer() {
        boolean confirme = AlertUtil.confirmation("Confirmation", "Relancer l'entraînement du modèle ? Cette opération peut prendre du temps.");
        if (!confirme) {
            return;
        }
        boutonReentrainer.setDisable(true);
        new Thread(() -> {
            try {
                intelligenceApi.reentrainer();
                Platform.runLater(() -> {
                    boutonReentrainer.setDisable(false);
                    AlertUtil.information("Succès", "Le modèle a été réentraîné.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonReentrainer.setDisable(false);
                    AlertUtil.erreur("Erreur", "Impossible de réentraîner le modèle.");
                });
            }
        }).start();
    }

    private void afficherErreurPrediction(String message) {
        libelleErreurPrediction.setText(message);
        libelleErreurPrediction.setVisible(true);
        libelleErreurPrediction.setManaged(true);
    }

    private void masquerErreurPrediction() {
        libelleErreurPrediction.setVisible(false);
        libelleErreurPrediction.setManaged(false);
    }
}