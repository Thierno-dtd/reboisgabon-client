package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.intelligence.PredictionSurvieReponse;
import com.reboisgabon.client.dto.intelligence.PredictionSurvieRequete;
import com.reboisgabon.client.util.JsonMapper;

import java.util.HashMap;

public class IntelligenceApi {

    public PredictionSurvieReponse predireSurvie(PredictionSurvieRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("intelligence/predire-survie/", requete);
        return JsonMapper.instance().readValue(reponse.body(), PredictionSurvieReponse.class);
    }

    public JsonNode recommanderEssence(String province, int topN) throws Exception {
        var reponse = ApiClient.getInstance().get("intelligence/recommander-essence/?province=" + java.net.URLEncoder.encode(province, java.nio.charset.StandardCharsets.UTF_8) + "&top_n=" + topN);
        return JsonMapper.instance().readTree(reponse.body());
    }

    public JsonNode detectionRisque(int page) throws Exception {
        var reponse = ApiClient.getInstance().get("intelligence/detection-risque/?page=" + page);
        return JsonMapper.instance().readTree(reponse.body());
    }

    public void reentrainer() throws Exception {
        ApiClient.getInstance().post("intelligence/reentrainer/", new HashMap<>());
    }
}