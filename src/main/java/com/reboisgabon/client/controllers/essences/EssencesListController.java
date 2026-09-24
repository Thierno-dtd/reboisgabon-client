package com.reboisgabon.client.controllers.essences;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.DashboardApi;
import com.reboisgabon.client.api.endpoints.EssencesApi;
import com.reboisgabon.client.dto.essences.Essence;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.Icones;
import com.reboisgabon.client.ui.Illustrations;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.ErreurApiUtil;
import com.reboisgabon.client.util.EssenceCache;
import com.reboisgabon.client.util.FiltreAutoUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class EssencesListController implements Initializable {

    @FXML private TextField champRecherche;
    @FXML private Button boutonNouvelleEssence;
    @FXML private Label libelleNombre;
    @FXML private FlowPane planche;

    private final EssencesApi essencesApi = new EssencesApi();
    private final DashboardApi dashboardApi = new DashboardApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        boolean peutCreer = SessionManager.getInstance().peutAcceder("essences", "create");
        boutonNouvelleEssence.setVisible(peutCreer);
        boutonNouvelleEssence.setManaged(peutCreer);
        FiltreAutoUtil.surSaisie(champRecherche, this::rechercher);
        rechercher();
    }

    @FXML
    private void rechercher() {
        String recherche = champRecherche.getText() == null ? "" : champRecherche.getText();
        new Thread(() -> {
            try {
                List<Essence> essences = essencesApi.rechercher(recherche).getResults();
                Map<String, JsonNode> statistiques = new HashMap<>();
                try {
                    for (JsonNode e : dashboardApi.essences()) {
                        statistiques.put(e.path("essence").asText(), e);
                    }
                } catch (Exception ignore) {
                }
                Platform.runLater(() -> afficher(essences, statistiques));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Chargement impossible", "Les essences n'ont pas pu être chargées."));
            }
        }).start();
    }

    private void afficher(List<Essence> essences, Map<String, JsonNode> statistiques) {
        planche.getChildren().clear();
        libelleNombre.setText(essences.size() + " espèce" + (essences.size() > 1 ? "s" : "") + " au référentiel");
        if (essences.isEmpty()) {
            planche.getChildren().add(Composants.etatVide("Aucune essence", "Ajoutez les espèces plantées dans le programme."));
            return;
        }
        int numero = 1;
        for (Essence essence : essences) {
            planche.getChildren().add(specimen(essence, statistiques.get(essence.getNom()), numero++));
        }
    }

    private VBox specimen(Essence essence, JsonNode stat, int numero) {
        Label reference = new Label(String.format("HERBIER RG · N° %03d", numero));
        reference.getStyleClass().add("cle-etiquette");
        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);
        HBox tete = new HBox(reference, espace);
        tete.setAlignment(Pos.CENTER_LEFT);
        boolean peutModifier = SessionManager.getInstance().peutAcceder("essences", "edit");
        boolean peutSupprimer = SessionManager.getInstance().peutAcceder("essences", "delete");
        if (peutModifier) {
            Button modifier = Composants.boutonIcone(Icones.MODIFIER, "Modifier", null);
            modifier.setOnAction(e -> ouvrirModification(essence));
            tete.getChildren().add(modifier);
        }
        if (peutSupprimer) {
            Button supprimer = Composants.boutonIcone(Icones.SUPPRIMER, "Supprimer", "bouton-icone-danger");
            supprimer.setOnAction(e -> supprimer(essence));
            tete.getChildren().add(supprimer);
        }

        StackPane dessin = new StackPane(Illustrations.feuille(Illustrations.especePour(essence.getNom()), 150,
                Color.web("#1F5136"), Color.web("#E4EFDE")));
        dessin.setMinHeight(170);
        dessin.setPadding(new Insets(6, 0, 6, 0));

        Label nom = new Label(essence.getNom());
        nom.getStyleClass().add("titre-section");
        nom.setStyle("-fx-font-size: 19px;");
        Label latin = new Label(essence.getNomScientifique() == null || essence.getNomScientifique().isBlank() ? "—" : essence.getNomScientifique());
        latin.getStyleClass().add("nom-scientifique");
        Label croissance = stat != null && stat.path("croissance_rapide").asBoolean()
                ? Composants.pastille("Croissance rapide", "or")
                : Composants.pastille("Croissance lente", "neutre");

        VBox etiquette = Composants.etiquetteSpecimen("Relevé de terrain", "", List.of(
                new Composants.Champ("Campagnes", stat == null ? "0" : Composants.nombre(stat.path("nombre_campagnes").asInt())),
                new Composants.Champ("Plants", stat == null ? "0" : Composants.nombre(stat.path("total_plants").asLong()))
        ));
        Double taux = stat != null && stat.hasNonNull("taux_survie_moyen") ? stat.get("taux_survie_moyen").asDouble() : null;
        Label cleSurvie = new Label("SURVIE");
        cleSurvie.getStyleClass().add("cle-etiquette");
        cleSurvie.setMinWidth(92);
        HBox ligneSurvie = new HBox(8, cleSurvie, Composants.barreSurvie(taux, 70));
        ligneSurvie.setAlignment(Pos.CENTER_LEFT);
        ligneSurvie.getStyleClass().add("ligne-etiquette");
        etiquette.getChildren().add(ligneSurvie);

        VBox feuille = new VBox(8, tete, dessin, nom, latin, croissance, etiquette);
        feuille.getStyleClass().add("feuille");
        feuille.setPadding(new Insets(12, 14, 14, 14));
        feuille.setPrefWidth(270);
        feuille.setMaxWidth(270);
        return feuille;
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<EssenceFormController>ouvrirModal("/com/reboisgabon/client/fxml/essence-form.fxml", "Nouvelle essence",
                controleur -> controleur.definirOnSucces(this::rafraichirApresModification));
    }

    private void ouvrirModification(Essence essence) {
        DialogUtil.<EssenceFormController>ouvrirModal("/com/reboisgabon/client/fxml/essence-form.fxml", "Modifier l'essence",
                controleur -> {
                    controleur.definirEssenceExistante(essence);
                    controleur.definirOnSucces(this::rafraichirApresModification);
                });
    }

    private void rafraichirApresModification() {
        EssenceCache.getInstance().invalider();
        rechercher();
    }

    private void supprimer(Essence essence) {
        boolean confirme = AlertUtil.confirmation("Supprimer cette essence ?",
                "« " + essence.getNom() + " » sera retirée du référentiel. Une essence déjà utilisée par des campagnes ne peut pas être supprimée.");
        if (!confirme) {
            return;
        }
        new Thread(() -> {
            try {
                essencesApi.supprimer(essence.getId());
                Platform.runLater(this::rafraichirApresModification);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Suppression refusée", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Serveur injoignable", "Impossible de joindre le serveur."));
            }
        }).start();
    }
}
