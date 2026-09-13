package com.reboisgabon.client.controllers.essences;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.EssencesApi;
import com.reboisgabon.client.dto.essences.Essence;
import com.reboisgabon.client.dto.essences.EssenceRequete;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EssenceFormController {

    @FXML private Label libelleTitre;
    @FXML private TextField champNom;
    @FXML private TextField champNomScientifique;
    @FXML private TextArea champDescription;
    @FXML private Label libelleErreur;
    @FXML private Button boutonEnregistrer;

    private final EssencesApi essencesApi = new EssencesApi();
    private Essence essenceExistante;
    private Runnable onSucces;

    public void definirEssenceExistante(Essence essence) {
        this.essenceExistante = essence;
        libelleTitre.setText("Modifier l'essence");
        champNom.setText(essence.getNom());
        champNomScientifique.setText(essence.getNomScientifique());
        champDescription.setText(essence.getDescription());
    }

    public void definirOnSucces(Runnable onSucces) {
        this.onSucces = onSucces;
    }

    @FXML
    private void enregistrer() {
        if (champNom.getText() == null || champNom.getText().isBlank()) {
            afficherErreur("Le nom est obligatoire.");
            return;
        }
        EssenceRequete requete = new EssenceRequete();
        requete.setNom(champNom.getText());
        requete.setNomScientifique(champNomScientifique.getText());
        requete.setDescription(champDescription.getText());
        masquerErreur();
        boutonEnregistrer.setDisable(true);
        new Thread(() -> {
            try {
                if (essenceExistante == null) {
                    essencesApi.creer(requete);
                } else {
                    essencesApi.modifier(essenceExistante.getId(), requete);
                }
                Platform.runLater(() -> {
                    if (onSucces != null) {
                        onSucces.run();
                    }
                    fermer();
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonEnregistrer.setDisable(false);
                    afficherErreur(ErreurApiUtil.message(e));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonEnregistrer.setDisable(false);
                    afficherErreur("Impossible de joindre le serveur.");
                });
            }
        }).start();
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        ((Stage) champNom.getScene().getWindow()).close();
    }

    private void afficherErreur(String message) {
        libelleErreur.setText(message);
        libelleErreur.setVisible(true);
        libelleErreur.setManaged(true);
    }

    private void masquerErreur() {
        libelleErreur.setVisible(false);
        libelleErreur.setManaged(false);
    }
}