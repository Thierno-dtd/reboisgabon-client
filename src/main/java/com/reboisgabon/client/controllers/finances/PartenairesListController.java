package com.reboisgabon.client.controllers.finances;

import com.reboisgabon.client.ui.Cellules;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.api.endpoints.PartenairesApi;
import com.reboisgabon.client.dto.finances.Partenaire;
import com.reboisgabon.client.dto.finances.TypePartenaire;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.PartenaireCache;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import com.reboisgabon.client.util.BoutonIconeUtil;
import com.reboisgabon.client.util.FiltreAutoUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PartenairesListController implements Initializable {

    @FXML private TextField champRecherche;
    @FXML private ComboBox<TypePartenaire> comboType;
    @FXML private Button boutonNouveauPartenaire;
    @FXML private TableView<Partenaire> tablePartenaires;
    @FXML private TableColumn<Partenaire, String> colonneNom;
    @FXML private TableColumn<Partenaire, String> colonneType;
    @FXML private TableColumn<Partenaire, String> colonnePays;
    @FXML private TableColumn<Partenaire, Boolean> colonneActif;
    @FXML private TableColumn<Partenaire, Void> colonneActions;

    private final PartenairesApi partenairesApi = new PartenairesApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboType.getItems().add(null);
        comboType.getItems().addAll(TypePartenaire.values());

        colonneNom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getNom()));

        colonneType.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getTypePartenaire() != null
                                ? data.getValue().getTypePartenaire().toString()
                                : ""
                ));

        colonnePays.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getPays()));

        colonneActif.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(
                        data.getValue().isActif()
                ));

        construireColonneActions();
        Cellules.rendu(colonneNom, Cellules::principal);
        Cellules.rendu(colonneType, v -> {
            String t = v.toString();
            return switch (t) {
                case "BAILLEUR_INTL" -> Composants.pastille("Bailleur international", "ocean");
                case "ETAT" -> Composants.pastille("État", "or");
                case "ONG" -> Composants.pastille("ONG", "canopee");
                case "ENTREPRISE" -> Composants.pastille("Entreprise", "laterite");
                default -> Composants.pastille(t, "neutre");
            };
        });
        Cellules.rendu(colonneActif, v -> Boolean.TRUE.equals(v) || "true".equals(v.toString()) ? Composants.pastille("Actif", "foret") : Composants.pastille("Inactif", "neutre"));
        Cellules.preparer(tablePartenaires, "Aucun partenaire", "Enregistrez les bailleurs et partenaires qui financent le programme.");

        boolean peutCreer = SessionManager.getInstance().peutAcceder("finances", "create");
        boutonNouveauPartenaire.setVisible(peutCreer);
        boutonNouveauPartenaire.setManaged(peutCreer);

        FiltreAutoUtil.surSaisie(champRecherche, this::rechercher);
        FiltreAutoUtil.surValeur(comboType, this::rechercher);

        rechercher();
    }

    private void construireColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {

            private final Button boutonModifier = BoutonIconeUtil.creer("✏️", "Modifier");

            {
                boutonModifier.setOnAction(evenement -> ouvrirModification(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                if (vide) {
                    setGraphic(null);
                    return;
                }
                boolean peutModifier = SessionManager.getInstance().peutAcceder("finances", "edit");
                boutonModifier.setVisible(peutModifier);
                boutonModifier.setManaged(peutModifier);
                setGraphic(boutonModifier);
            }
        });
    }

    @FXML
    private void rechercher() {
        Map<String, String> filtres = new HashMap<>();
        filtres.put("search", champRecherche.getText());
        if (comboType.getValue() != null) {
            filtres.put("type_partenaire", comboType.getValue().toString());
        }
        new Thread(() -> {
            try {
                var page = partenairesApi.rechercher(filtres);
                Platform.runLater(() -> tablePartenaires.getItems().setAll(page.getResults()));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les partenaires."));
            }
        }).start();
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<PartenaireFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/partenaire-form.fxml",
                "Nouveau partenaire",
                controleur -> controleur.definirOnSucces(this::rafraichir));
    }

    private void ouvrirModification(Partenaire partenaire) {
        DialogUtil.<PartenaireFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/partenaire-form.fxml",
                "Modifier le partenaire",
                controleur -> {
                    controleur.definirPartenaireExistant(partenaire);
                    controleur.definirOnSucces(this::rafraichir);
                });
    }

    private void rafraichir() {
        PartenaireCache.getInstance().invalider();
        rechercher();
    }
}