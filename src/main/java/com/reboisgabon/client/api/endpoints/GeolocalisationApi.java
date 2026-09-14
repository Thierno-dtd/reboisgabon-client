package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.util.JsonMapper;

public class GeolocalisationApi {

    public String sitesGeojson() throws Exception {
        var reponse = ApiClient.getInstance().get("sites-geojson/");
        return reponse.body();
    }

    public JsonNode sitesProximite(double latitude, double longitude, double rayonKm, int page) throws Exception {
        var reponse = ApiClient.getInstance().get(
                "sites-proximite/?lat=" + latitude + "&lon=" + longitude + "&rayon_km=" + rayonKm + "&page=" + page);
        return JsonMapper.instance().readTree(reponse.body());
    }
}