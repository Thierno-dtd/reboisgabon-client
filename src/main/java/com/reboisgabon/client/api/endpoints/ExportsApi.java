package com.reboisgabon.client.api.endpoints;

import com.reboisgabon.client.api.ApiClient;

public class ExportsApi {

    public byte[] rapportSynthesePdf() {
        return ApiClient.getInstance().telechargerBinaire("exports/rapport-synthese/pdf/").body();
    }

    public byte[] rapportFinancierPdf() {
        return ApiClient.getInstance().telechargerBinaire("exports/rapport-financier/pdf/").body();
    }

    public byte[] sitesExcel() {
        return ApiClient.getInstance().telechargerBinaire("exports/sites/excel/").body();
    }

    public byte[] campagnesExcel() {
        return ApiClient.getInstance().telechargerBinaire("exports/campagnes/excel/").body();
    }

    public byte[] financementsExcel() {
        return ApiClient.getInstance().telechargerBinaire("exports/financements/excel/").body();
    }
}