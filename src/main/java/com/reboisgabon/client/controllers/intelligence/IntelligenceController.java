package com.reboisgabon.client.controllers.intelligence;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.IntelligenceApi;
import com.reboisgabon.client.controllers.sites.SitesListController;
import com.reboisgabon.client.dto.essences.Essence;
import com.reboisgabon.client.dto.intelligence.PredictionSurvieReponse;
import com.reboisgabon.client.dto.intelligence.PredictionSurvieRequete;
import com.reboisgabon.client.session.Role;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.Icones;
import com.reboisgabon.client.ui.Illustrations;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.EssenceCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class IntelligenceController implements Initializable {

    @FXML private Button boutonReentrainer;
    @FXML private ComboBox<Essence> comboEssence;
    @FXML private ComboBox<String> comboProvince;
    @FXML private TextField champSuperficie;
    @FXML private TextField champNombrePlants;
    @FXML private ComboBox<Integer> comboMois;
    @FXML private CheckBox caseCroissanceRapide;
    @FXML private Button boutonPredire;
    @FXML private Label libelleErreurPrediction;
    @FXML private VBox conteneurResultatPrediction;
    @FXML private ComboBox<String> comboProvinceRecommandation;
    @FXML private VBox conteneurRecommandation;
    @FXML private VBox conteneurDetectionRisque;

    private final IntelligenceApi intelligenceApi = new IntelligenceApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboEssence.setConverter(convertisseur(e -> e == null ? "" : e.getNom()));
        EssenceCache.getInstance().assurerCharge(essences -> comboEssence.getItems().setAll(essences));
        comboProvince.getItems().setAll(SitesListController.PROVINCES);
        comboProvinceRecommandation.getItems().setAll(SitesListController.PROVINCES);
        for (int m = 1; m <= 12; m++) {
            comboMois.getItems().add(m);
        }
        comboMois.setConverter(convertisseur(m -> m == null ? "" : capitaliser(Month.of(m).getDisplayName(TextStyle.FULL, Locale.FRANCE))));
        comboMois.setValue(LocalDate.now().getMonthValue());

        Role role = SessionManager.getInstance().getRole();
        boolean peutReentrainer = role == Role.ADMIN || role == Role.SUPERVISEUR;
        boutonReentrainer.setVisible(peutReentrainer);
        boutonReentrainer.setManaged(peutReentrainer);

        afficherAttentePrediction();
        comboProvinceRecommandation.valueProperty().addListener((o, a, n) -> recommander());
        comboProvinceRecommandation.setValue("Estuaire");
        chargerDetectionRisque();
    }

    private static <T> StringConverter<T> convertisseur(java.util.function.Function<T, String> libelle) {
        return new StringConverter<>() {
            @Override
            public String toString(T objet) {
                return libelle.apply(objet);
            }

            @Override
            public T fromString(String texte) {
                return null;
            }
        };
    }

    private String capitaliser(String t) {
        return t.isEmpty() ? t : t.substring(0, 1).toUpperCase(Locale.FRANCE) + t.substring(1);
    }

    private void afficherAttentePrediction() {
        StackPane dessin = new StackPane(Illustrations.feuille(Illustrations.Espece.PADOUK, 120, Color.web("#8FA894"), Color.web("#EEF3EA")));
        dessin.setPadding(new Insets(10, 0, 6, 0));
        Label texte = new Label("Le résultat de la simulation s'affichera ici.");
        texte.getStyleClass().add("texte-muet");
        texte.setWrapText(true);
        VBox bloc = new VBox(8, dessin, texte);
        bloc.setAlignment(Pos.CENTER);
        bloc.setStyle("-fx-background-color: #F6F8F3; -fx-background-radius: 6;");
        bloc.setPadding(new Insets(14));
        conteneurResultatPrediction.getChildren().setAll(bloc);
    }

    @FXML
    private void predire() {
        if (comboEssence.getValue() == null || comboProvince.getValue() == null) {
            afficherErreurPrediction("Choisissez l'essence et la province du projet.");
            return;
        }
        PredictionSurvieRequete requete = new PredictionSurvieRequete();
        requete.setEssence(comboEssence.getValue().getNom());
        requete.setProvince(comboProvince.getValue());
        requete.setCroissanceRapide(caseCroissanceRapide.isSelected());
        try {
            requete.setSuperficieSite(new BigDecimal(champSuperficie.getText().replace(",", ".").replaceAll("\\s", "")));
            requete.setNombrePlants(Integer.parseInt(champNombrePlants.getText().replaceAll("[\\s\\u202F]", "")));
            requete.setMoisPlantation(comboMois.getValue());
        } catch (Exception e) {
            afficherErreurPrediction("Indiquez une superficie et un nombre de plants valides.");
            return;
        }
        masquerErreurPrediction();
        boutonPredire.setDisable(true);
        boutonPredire.setText("Calcul en cours…");
        new Thread(() -> {
            try {
                PredictionSurvieReponse reponse = intelligenceApi.predireSurvie(requete);
                Platform.runLater(() -> {
                    reinitialiserBouton();
                    afficherResultatPrediction(reponse);
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    reinitialiserBouton();
                    afficherErreurPrediction(e.getStatutHttp() == 503
                            ? "Le modèle n'est pas encore entraîné sur le serveur. Lancez un réentraînement."
                            : "La prédiction n'a pas pu être calculée pour ces paramètres.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    reinitialiserBouton();
                    afficherErreurPrediction("Impossible de joindre le serveur.");
                });
            }
        }).start();
    }

    private void reinitialiserBouton() {
        boutonPredire.setDisable(false);
        boutonPredire.setText("Estimer la survie");
    }

    private void afficherResultatPrediction(PredictionSurvieReponse reponse) {
        double taux = reponse.getTauxSurviePredit() != null ? reponse.getTauxSurviePredit().doubleValue() : 0;
        Label cle = new Label("SURVIE ATTENDUE");
        cle.getStyleClass().add("cle-etiquette");
        Label valeur = new Label(Composants.pourcentage(taux));
        valeur.getStyleClass().add("chiffre-fort");
        valeur.setStyle("-fx-font-size: 38px; -fx-text-fill: " + Composants.hexSurvie(taux) + ";");
        HBox barre = Composants.barreProgression(taux / 100.0, 200, Composants.couleurSurvie(taux));
        Label verdict = reponse.isCampagneARisque()
                ? Composants.pastille("Campagne à risque", "rouge")
                : Composants.pastille("Survie jugée satisfaisante", "foret");
        Label conseil = new Label(reponse.isCampagneARisque()
                ? "Envisagez une autre essence, une autre saison de plantation ou un suivi rapproché."
                : "Planifiez un premier contrôle de terrain trois mois après la mise en terre.");
        conseil.getStyleClass().add("texte-petit");
        conseil.setWrapText(true);
        Label confiance = new Label(reponse.getNiveauConfiance());
        confiance.getStyleClass().add("texte-petit");
        confiance.setWrapText(true);
        confiance.setStyle("-fx-font-style: italic;");
        VBox bloc = new VBox(8, cle, valeur, barre, verdict, conseil, confiance);
        bloc.setStyle("-fx-background-color: #F6F8F3; -fx-background-radius: 6; -fx-border-color: #DDE3D6; -fx-border-radius: 6;");
        bloc.setPadding(new Insets(14));
        conteneurResultatPrediction.getChildren().setAll(bloc);
    }

    private void recommander() {
        String province = comboProvinceRecommandation.getValue();
        if (province == null) {
            return;
        }
        conteneurRecommandation.getChildren().setAll(Composants.chargement("Analyse de l'historique…"));
        new Thread(() -> {
            try {
                JsonNode resultat = intelligenceApi.recommanderEssence(province, 5);
                Platform.runLater(() -> afficherRecommandations(resultat));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label message = new Label("Les recommandations n'ont pas pu être chargées.");
                    message.getStyleClass().add("message-erreur");
                    conteneurRecommandation.getChildren().setAll(message);
                });
            }
        }).start();
    }

    private void afficherRecommandations(JsonNode resultat) {
        VBox lignes = new VBox();
        int rang = 1;
        for (JsonNode r : resultat.path("recommandations")) {
            String nom = r.path("essence").asText();
            StackPane vignette = new StackPane(Illustrations.feuille(Illustrations.especePour(nom), 34, Color.web("#1F5136"), Color.web("#E4EFDE")));
            vignette.setMinSize(40, 40);
            vignette.setMaxSize(40, 40);
            vignette.setStyle("-fx-background-color: #F4F7F1; -fx-background-radius: 6;");
            Label rangLabel = new Label(String.valueOf(rang++));
            rangLabel.getStyleClass().add("chiffre-moyen");
            rangLabel.setMinWidth(18);
            Label libelle = new Label(nom);
            libelle.getStyleClass().add("titre-3");
            Label detail = new Label(r.path("nombre_campagnes_analysees").asInt() + " campagnes analysées"
                    + (r.path("croissance_rapide").asBoolean() ? "  ·  croissance rapide" : ""));
            detail.getStyleClass().add("texte-petit");
            VBox textes = new VBox(1, libelle, detail);
            HBox.setHgrow(textes, Priority.ALWAYS);
            HBox ligne = new HBox(10, rangLabel, vignette, textes,
                    Composants.barreSurvie(r.hasNonNull("taux_survie_moyen_historique") ? r.get("taux_survie_moyen_historique").asDouble() : null, 56));
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.getStyleClass().add("ligne-liste");
            lignes.getChildren().add(ligne);
        }
        if (lignes.getChildren().isEmpty()) {
            lignes.getChildren().add(Composants.etatVide("Pas encore d'historique",
                    "Aucune campagne contrôlée dans cette province : le modèle ne peut pas encore y comparer les essences."));
        }
        conteneurRecommandation.getChildren().setAll(lignes);
    }

    private void chargerDetectionRisque() {
        new Thread(() -> {
            try {
                JsonNode resultat = intelligenceApi.detectionRisque(1);
                Platform.runLater(() -> afficherDetection(resultat));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label message = new Label("La détection des risques n'a pas pu être chargée.");
                    message.getStyleClass().add("message-erreur");
                    conteneurDetectionRisque.getChildren().setAll(message);
                });
            }
        }).start();
    }

    private void afficherDetection(JsonNode resultat) {
        int nombre = resultat.path("nombre_campagnes_a_risque_predit").asInt();
        Label titre = new Label("Campagnes à risque détectées par le modèle");
        titre.getStyleClass().add("titre-section");
        Label sous = new Label("Campagnes récentes dont la survie prédite passe sous le seuil d'alerte, avant même le premier contrôle.");
        sous.getStyleClass().add("texte-muet");
        sous.setWrapText(true);
        VBox textes = new VBox(2, titre, sous);
        HBox.setHgrow(textes, Priority.ALWAYS);
        HBox entete = new HBox(10, textes, Composants.pastille(nombre + " campagne" + (nombre > 1 ? "s" : ""), nombre > 0 ? "rouge" : "foret"));
        entete.setAlignment(Pos.CENTER_LEFT);
        entete.getStyleClass().add("feuille-titre");

        VBox lignes = new VBox();
        lignes.setPadding(new Insets(4, 16, 12, 16));
        for (JsonNode c : resultat.path("campagnes").path("results")) {
            Label site = new Label(c.path("site").asText(c.path("site_nom").asText("Campagne")));
            site.getStyleClass().add("titre-3");
            Label detail = new Label(c.path("essence").asText(c.path("essence_nom").asText("")) + "  ·  plantée le "
                    + Composants.date(c.path("date_plantation").asText("")));
            detail.getStyleClass().add("texte-petit");
            VBox t = new VBox(1, site, detail);
            HBox.setHgrow(t, Priority.ALWAYS);
            Double taux = c.hasNonNull("taux_survie_predit") ? c.get("taux_survie_predit").asDouble() : null;
            HBox ligne = new HBox(10, Icones.icone(Icones.ALERTE, 18, Color.web("#B3412E")), t, Composants.barreSurvie(taux, 60));
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.getStyleClass().add("ligne-liste");
            lignes.getChildren().add(ligne);
        }
        if (lignes.getChildren().isEmpty()) {
            lignes.getChildren().add(Composants.etatVide("Aucune campagne à risque",
                    "Selon le modèle, toutes les campagnes récentes devraient dépasser le seuil de survie attendu."));
        }
        conteneurDetectionRisque.getChildren().setAll(entete, lignes);
    }

    @FXML
    private void reentrainer() {
        boolean confirme = AlertUtil.confirmation("Réentraîner le modèle ?",
                "Le modèle sera recalculé à partir de tous les contrôles enregistrés. L'opération peut prendre quelques secondes.");
        if (!confirme) {
            return;
        }
        boutonReentrainer.setDisable(true);
        new Thread(() -> {
            try {
                intelligenceApi.reentrainer();
                Platform.runLater(() -> {
                    boutonReentrainer.setDisable(false);
                    AlertUtil.information("Modèle mis à jour", "Le modèle a été réentraîné avec les dernières données de terrain.");
                    chargerDetectionRisque();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonReentrainer.setDisable(false);
                    AlertUtil.erreur("Réentraînement impossible", "Le serveur n'a pas pu réentraîner le modèle.");
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
