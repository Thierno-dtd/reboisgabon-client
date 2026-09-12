package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.util.JsonMapper;

public class DashboardApi {

    public JsonNode overview() throws Exception {
        return lire("dashboard/overview/");
    }

    public JsonNode sites(int page) throws Exception {
        return lire("dashboard/sites/?page=" + page);
    }

    public JsonNode essences() throws Exception {
        return lire("dashboard/essences/");
    }

    public JsonNode provinces() throws Exception {
        return lire("dashboard/provinces/");
    }

    public JsonNode evolution() throws Exception {
        return lire("dashboard/evolution/");
    }

    public JsonNode alertes(int pageCritiques, int pageSansSuivi) throws Exception {
        return lire("dashboard/alertes/?page_critiques=" + pageCritiques + "&page_sans_suivi=" + pageSansSuivi);
    }

    public JsonNode responsables() throws Exception {
        return lire("dashboard/responsables/");
    }

    public JsonNode financier() throws Exception {
        return lire("dashboard/financier/");
    }

    public JsonNode objectifs() throws Exception {
        return lire("dashboard/objectifs/");
    }

    public JsonNode scoresEcologiques(int page) throws Exception {
        return lire("dashboard/scores-ecologiques/?page=" + page);
    }

    public JsonNode carteProvinces() throws Exception {
        return lire("dashboard/carte-provinces/");
    }

    public JsonNode comparaisonPeriode(String type) throws Exception {
        return lire("dashboard/comparaison-periode/?type=" + type);
    }

    private JsonNode lire(String chemin) throws Exception {
        var reponse = ApiClient.getInstance().get(chemin);
        return JsonMapper.instance().readTree(reponse.body());
    }
}