package com.reboisgabon.client.controllers.finances;

import com.reboisgabon.client.api.endpoints.ExportsApi;
import com.reboisgabon.client.ui.ExportUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class FinancesController {

    @FXML private Button boutonRapportFinancier;
    @FXML private Button boutonFinancementsExcel;

    private final ExportsApi exportsApi = new ExportsApi();

    @FXML
    private void exporterRapportFinancier() {
        ExportUtil.pdf(boutonRapportFinancier, "rapport-financier-reboisgabon", exportsApi::rapportFinancierPdf);
    }

    @FXML
    private void exporterFinancements() {
        ExportUtil.excel(boutonFinancementsExcel, "financements-reboisgabon", exportsApi::financementsExcel);
    }
}
