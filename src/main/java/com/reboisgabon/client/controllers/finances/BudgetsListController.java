package com.reboisgabon.client.controllers.finances;

import com.reboisgabon.client.api.endpoints.BudgetsApi;
import com.reboisgabon.client.dto.campagnes.Campagne;
import com.reboisgabon.client.dto.finances.BudgetCampagne;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.CampagneCache;
import com.reboisgabon.client.util.DialogUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import com.reboisgabon.client.util.FiltreAutoUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class BudgetsListController implements Initializable {

    @FXML private ComboBox<Campagne> comboCampagne;
    @FXML private Button boutonNouveauBudget;
    @FXML private TableView<BudgetCampagne> tableBudgets;
    @FXML private TableColumn<BudgetCampagne, String> colonneCampagne;
    @FXML private TableColumn<BudgetCampagne, String> colonneMontantAlloue;
    @FXML private TableColumn<BudgetCampagne, String> colonneMontantReel;

    private final BudgetsApi budgetsApi = new BudgetsApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboCampagne.setConverter(new StringConverter<>() {
            @Override
            public String toString(Campagne campagne) {
                return campagne != null ? CampagneCache.getInstance().libelleDe(campagne.getId()) : "";
            }

            @Override
            public Campagne fromString(String texte) {
                return null;
            }
        });
        CampagneCache.getInstance().assurerCharge(campagnes -> {
            comboCampagne.getItems().add(null);
            comboCampagne.getItems().addAll(campagnes);
        });
        colonneCampagne.setCellValueFactory(data -> new ReadOnlyStringWrapper(CampagneCache.getInstance().libelleDe(data.getValue().getCampagne())));
        colonneMontantAlloue.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getMontantAlloue() != null
                                ? data.getValue().getMontantAlloue().toString()
                                : "—"
                ));

        colonneMontantReel.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getMontantReel() != null
                                ? data.getValue().getMontantReel().toString()
                                : "—"
                ));
        boolean peutCreer = SessionManager.getInstance().peutAcceder("finances", "create");
        boutonNouveauBudget.setVisible(peutCreer);
        boutonNouveauBudget.setManaged(peutCreer);

        FiltreAutoUtil.surValeur(comboCampagne, this::rechercher);

        rechercher();
    }

    @FXML
    private void rechercher() {
        String campagneId = comboCampagne.getValue() != null ? comboCampagne.getValue().getId() : null;
        new Thread(() -> {
            try {
                var page = budgetsApi.rechercher(campagneId);
                Platform.runLater(() -> tableBudgets.getItems().setAll(page.getResults()));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les budgets."));
            }
        }).start();
    }

    @FXML
    private void ouvrirCreation() {
        DialogUtil.<BudgetFormController>ouvrirModal(
                "/com/reboisgabon/client/fxml/budget-form.fxml",
                "Nouveau budget",
                controleur -> controleur.definirOnSucces(this::rechercher));
    }
}