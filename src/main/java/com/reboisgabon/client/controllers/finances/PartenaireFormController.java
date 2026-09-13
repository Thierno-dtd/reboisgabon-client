package com.reboisgabon.client.controllers.finances;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.PartenairesApi;
import com.reboisgabon.client.dto.finances.Partenaire;
import com.reboisgabon.client.dto.finances.PartenaireRequete;
import com.reboisgabon.client.dto.finances.TypePartenaire;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PartenaireFormController implements Initializable {

    @FXML private Label libelleTitre;
    @FXML private TextField champNom;
    @FXML private ComboBox<TypePartenaire> comboType;
    @FXML private TextField champPays;
    @FXML private CheckBox caseActif;
    @FXML private Label libelleErreur;
    @FXML private Button boutonEnregistrer;

    private final PartenairesApi partenairesApi = new PartenairesApi();
    private Partenaire partenaireExistant;
    private Runnable onSucces;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboType.getItems().setAll(TypePartenaire.values());
    }

    public void definirPartenaireExistant(Partenaire partenaire) {
        this.partenaireExistant = partenaire;
        libelleTitre.setText("Modifier le partenaire");
        champNom.setText(partenaire.getNom());
        comboType.setValue(partenaire.getTypePartenaire());
        champPays.setText(partenaire.getPays());
        caseActif.setSelected(partenaire.isActif());
    }

    public void definirOnSucces(Runnable onSucces) {
        this.onSucces = onSucces;
    }

    @FXML
    private void enregistrer() {
        if (champNom.getText() == null || champNom.getText().isBlank() || comboType.getValue() == null) {
            afficherErreur("Le nom et le type sont obligatoires.");
            return;
        }
        PartenaireRequete requete = new PartenaireRequete();
        requete.setNom(champNom.getText());
        requete.setTypePartenaire(comboType.getValue());
        requete.setPays(champPays.getText());
        requete.setActif(caseActif.isSelected());
        masquerErreur();
        boutonEnregistrer.setDisable(true);
        new Thread(() -> {
            try {
                if (partenaireExistant == null) {
                    partenairesApi.creer(requete);
                } else {
                    partenairesApi.modifier(partenaireExistant.getId(), requete);
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