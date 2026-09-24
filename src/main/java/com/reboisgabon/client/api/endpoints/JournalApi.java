package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.util.JsonMapper;

import java.util.Map;

public class JournalApi {

    public JsonNode rechercher(Map<String, String> filtres) throws Exception {
        StringBuilder chemin = new StringBuilder("journal/?");
        filtres.forEach((cle, valeur) -> {
            if (valeur != null && !valeur.isBlank()) {
                chemin.append(cle).append("=").append(java.net.URLEncoder.encode(valeur.trim(), java.nio.charset.StandardCharsets.UTF_8)).append("&");
            }
        });
        var reponse = ApiClient.getInstance().get(chemin.toString());
        return JsonMapper.instance().readTree(reponse.body());
    }

    public JsonNode rechercherUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return JsonMapper.instance().readTree(reponse.body());
    }

    public JsonNode statistiques() throws Exception {
        var reponse = ApiClient.getInstance().get("journal/statistiques/");
        return JsonMapper.instance().readTree(reponse.body());
    }

    public byte[] exporterCsv() {
        return ApiClient.getInstance().telechargerBinaire("journal/export-csv/").body();
    }
}