package com.reboisgabon.client.controllers.suivis;

import com.reboisgabon.client.api.endpoints.PhotosSuiviApi;
import com.reboisgabon.client.config.AppConfig;
import com.reboisgabon.client.dto.suivis.PhotoSuivi;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class SuiviPhotosController {

    @FXML private Label libelleFichierSelectionne;
    @FXML private TextField champLegende;
    @FXML private Button boutonEnvoyer;
    @FXML private FlowPane grillePhotos;
    @FXML private Label libelleErreur;

    private final PhotosSuiviApi photosSuiviApi = new PhotosSuiviApi();
    private File fichierSelectionne;
    private String suiviId;

    public void definirSuiviId(String suiviId) {
        this.suiviId = suiviId;
        chargerPhotos();
    }

    @FXML
    private void choisirFichier() {
        FileChooser selecteur = new FileChooser();
        selecteur.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp"));
        File fichier = selecteur.showOpenDialog(((Stage) champLegende.getScene().getWindow()));
        if (fichier != null) {
            fichierSelectionne = fichier;
            libelleFichierSelectionne.setText(fichier.getName());
        }
    }

    @FXML
    private void envoyerPhoto() {
        if (fichierSelectionne == null) {
            afficherErreur("Veuillez choisir une image.");
            return;
        }
        masquerErreur();
        boutonEnvoyer.setDisable(true);
        new Thread(() -> {
            try {
                photosSuiviApi.uploader(suiviId, fichierSelectionne, champLegende.getText());
                Platform.runLater(() -> {
                    boutonEnvoyer.setDisable(false);
                    fichierSelectionne = null;
                    libelleFichierSelectionne.setText("");
                    champLegende.clear();
                    chargerPhotos();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonEnvoyer.setDisable(false);
                    afficherErreur("Échec de l'envoi (format accepté : jpg, jpeg, png, webp, 5 Mo max).");
                });
            }
        }).start();
    }

    private void chargerPhotos() {
        new Thread(() -> {
            try {
                var page = photosSuiviApi.lister(suiviId);
                Platform.runLater(() -> afficherPhotos(page.getResults()));
            } catch (Exception e) {
                Platform.runLater(() -> afficherErreur("Impossible de charger les photos."));
            }
        }).start();
    }

    private void afficherPhotos(List<PhotoSuivi> photos) {
        grillePhotos.getChildren().clear();
        for (PhotoSuivi photo : photos) {
            VBox vignette = new VBox(6);
            vignette.getStyleClass().add("vignette-photo");
            vignette.setPadding(new Insets(10));
            ImageView imageView = new ImageView(new Image(AppConfig.SERVER_BASE_URL + photo.getImage(), 180, 180, true, true, true));
            Label legende = new Label(photo.getLegende() != null ? photo.getLegende() : "");
            legende.getStyleClass().add("texte-muet");
            vignette.getChildren().addAll(imageView, legende);
            grillePhotos.getChildren().add(vignette);
        }
    }

    @FXML
    private void fermer() {
        ((Stage) champLegende.getScene().getWindow()).close();
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