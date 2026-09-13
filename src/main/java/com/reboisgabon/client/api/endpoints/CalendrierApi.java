package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.util.JsonMapper;

public class CalendrierApi {

    public JsonNode charger(int horizon, int pageAVenir, int pageEnRetard) throws Exception {
        var reponse = ApiClient.getInstance().get(
                "calendrier-suivis/?horizon=" + horizon + "&page_a_venir=" + pageAVenir + "&page_en_retard=" + pageEnRetard);
        return JsonMapper.instance().readTree(reponse.body());
    }
}