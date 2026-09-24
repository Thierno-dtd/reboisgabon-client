package com.reboisgabon.client.controllers.parametres;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.MeApi;
import com.reboisgabon.client.util.ErreurApiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ParametreMotDePasseController {

    @FXML private PasswordField champAncien;
    @FXML private PasswordField champNouveau;
    @FXML private PasswordField champConfirmation;
    @FXML private Label regleLongueur;
    @FXML private Label regleMajuscule;
    @FXML private Label regleChiffre;
    @FXML private Label regleIdentique;
    @FXML private Label libelleMessage;
    @FXML private Button boutonValider;

    private final MeApi meApi = new MeApi();

    @FXML
    private void initialize() {
        champNouveau.textProperty().addListener((o, a, n) -> evaluerRegles());
        champConfirmation.textProperty().addListener((o, a, n) -> evaluerRegles());
        evaluerRegles();
    }

    private boolean evaluerRegles() {
        String mdp = champNouveau.getText() == null ? "" : champNouveau.getText();
        boolean longueur = mdp.length() >= 8;
        boolean casse = mdp.chars().anyMatch(Character::isUpperCase) && mdp.chars().anyMatch(Character::isLowerCase);
        boolean chiffre = mdp.chars().anyMatch(Character::isDigit);
        boolean identique = !mdp.isEmpty() && mdp.equals(champConfirmation.getText());
        marquer(regleLongueur, longueur);
        marquer(regleMajuscule, casse);
        marquer(regleChiffre, chiffre);
        marquer(regleIdentique, identique);
        return longueur && casse && chiffre && identique;
    }

    private void marquer(Label regle, boolean respectee) {
        regle.setGraphic(com.reboisgabon.client.ui.Icones.icone(respectee ? com.reboisgabon.client.ui.Icones.VALIDE : "mdi2c-circle-outline", 15,
                javafx.scene.paint.Color.web(respectee ? "#1F5136" : "#8A978C")));
        regle.setStyle(respectee ? "-fx-text-fill: #1F5136;" : "");
    }

    @FXML
    private void changerMotDePasse() {
        if (champAncien.getText() == null || champAncien.getText().isBlank()
                || champNouveau.getText() == null || champNouveau.getText().isBlank()) {
            afficherMessage("Renseignez votre mot de passe actuel et le nouveau.", "message-erreur");
            return;
        }
        if (!evaluerRegles()) {
            afficherMessage("Le nouveau mot de passe ne respecte pas encore toutes les règles indiquées à droite.", "message-erreur");
            return;
        }
        masquerMessage();
        boutonValider.setDisable(true);
        new Thread(() -> {
            try {
                meApi.changerMotDePasse(champAncien.getText(), champNouveau.getText());
                Platform.runLater(() -> {
                    boutonValider.setDisable(false);
                    champAncien.clear();
                    champNouveau.clear();
                    champConfirmation.clear();
                    afficherMessage("Votre mot de passe a été mis à jour.", "message-succes");
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonValider.setDisable(false);
                    afficherMessage(ErreurApiUtil.message(e), "message-erreur");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonValider.setDisable(false);
                    afficherMessage("Impossible de joindre le serveur.", "message-erreur");
                });
            }
        }).start();
    }

    private void afficherMessage(String message, String classeStyle) {
        libelleMessage.getStyleClass().removeAll("message-erreur", "message-succes");
        libelleMessage.getStyleClass().add(classeStyle);
        libelleMessage.setText(message);
        libelleMessage.setVisible(true);
        libelleMessage.setManaged(true);
    }

    private void masquerMessage() {
        libelleMessage.setVisible(false);
        libelleMessage.setManaged(false);
    }
}