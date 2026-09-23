package com.reboisgabon.client.controllers.finances;

import com.reboisgabon.client.api.endpoints.FinancementsApi;
import com.reboisgabon.client.dto.finances.Devise;
import com.reboisgabon.client.dto.finances.Financement;
import com.reboisgabon.client.dto.finances.Partenaire;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.CampagneCache;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.PartenaireCache;
import com.reboisgabon.client.util.SiteCache;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.reboisgabon.client.util.FiltreAutoUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class FinancementsListController implements Initializable {

    @FXML private ComboBox<Partenaire> comboPartenaire;
    @FXML private ComboBox<Devise> comboDevise;
    @FXML private Button boutonNouveauFinancement;
    @FXML private TableView<Financement> tableFinancements;
    @FXML private TableColumn<Financement, String> colonnePartenaire;
    @FXML private TableColumn<Financement, String> colonneCible;
    @FXML private TableColumn<Financement, String> colonneMontant;
    @FXML private TableColumn<Financement, String> colonneDevise;
    @FXML private TableColumn<Financement, String> colonneDate;

    private final FinancementsApi financementsApi = new FinancementsApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboDevise.getItems().add(null);
        comboDevise.getItems().addAll(Devise.values());

        PartenaireCache.getInstance().assurerCharge(partenaires -> {
            comboPartenaire.getItems().add(null);
            comboPartenaire.getItems().addAll(partenaires);
        });

        colonnePartenaire.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        PartenaireCache.getInstance().nomDe(data.getValue().getPartenaire())
                ));

        colonneCible.setCellValueFactory(data -> {
            Financement financement = data.getValue();

            if (financement.getCampagne() != null) {
                return new ReadOnlyStringWrapper(
                        "Campagne — " + CampagneCache.getInstance().libelleDe(financement.getCampagne())
                );
            }

            if (financement.getSite() != null) {
                return new ReadOnlyStringWrapper(
                        "Site — " + SiteCache.getInstance().nomDe(financement.getSite())
                );
            }

            return new ReadOnlyStringWrapper("—");
        });

        colonneMontant.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getMontant() != null
                                ? data.getValue().getMontant().toString()
                                : ""
                ));

        colonneDevise.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getDevise() != null
                                ? data.getValue().getDevise().toString()
                                : ""
                ));

        colonneDate.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getDateFinancement() != null
                                ? data.getValue().getDateFinancement().toString()
                                : ""
                ));

        boolean peutCreer = SessionManager.getInstance().peutAcceder("finances", "create");
        boutonNouveauFinancement.setVisible(peutCreer);
        boutonNouveauFinancement.setManaged(peutCreer);

        FiltreAutoUtil.surValeur(comboPartenaire, this::rechercher);
        FiltreAutoUtil.surValeur(comboDevise, this::rechercher);

        rechercher();
    }

    @FXML
    private void rechercher() {
        Map<String, String> filtres = new HashMap<>();
        if (comboPartenaire.getValue() != null) {
            filtres.put("partenaire", comboPartenaire.getValue().getId());
        }
        if (comboDevise.getValue() != null) {
            filtres.put("devise", comboDevise.getValue().toString());
        }
        new Thread(() -> {
            try {
                var page = financementsApi.rechercher(filtres);
                Platform.runLater(() -> tableFinancements.getItems().setAll(page.getResults()));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les financements."));
            }
        }).start();
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<FinancementFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/financement-form.fxml",
                "Nouveau financement",
                controleur -> controleur.definirOnSucces(this::rechercher));
    }
}