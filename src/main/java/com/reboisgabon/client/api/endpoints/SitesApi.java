package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.dto.sites.SiteRequete;
import com.reboisgabon.client.util.JsonMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class SitesApi {

    public PageDrf<Site> rechercher(Map<String, String> filtres) throws Exception {
        StringBuilder chemin = new StringBuilder("sites/?");
        filtres.forEach((cle, valeur) -> {
            if (valeur != null && !valeur.isBlank()) {
                chemin.append(cle).append("=").append(java.net.URLEncoder.encode(valeur.trim(), java.nio.charset.StandardCharsets.UTF_8)).append("&");
            }
        });
        var reponse = ApiClient.getInstance().get(chemin.toString());
        return lirePage(reponse.body());
    }

    public PageDrf<Site> rechercherUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return lirePage(reponse.body());
    }

    public Site obtenir(String id) throws Exception {
        var reponse = ApiClient.getInstance().get("sites/" + id + "/");
        return JsonMapper.instance().readValue(reponse.body(), Site.class);
    }

    public Site creer(SiteRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("sites/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Site.class);
    }

    public Site modifier(String id, SiteRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().patch("sites/" + id + "/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Site.class);
    }

    public void supprimer(String id) throws Exception {
        ApiClient.getInstance().delete("sites/" + id + "/?confirm=true");
    }

    private PageDrf<Site> lirePage(String corps) throws Exception {
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, Site.class);
        return JsonMapper.instance().readValue(corps, type);
    }

    public JsonNode scoreEcologique(String id) throws Exception {
        var reponse = ApiClient.getInstance().get("sites/" + id + "/score-ecologique/");
        return JsonMapper.instance().readTree(reponse.body());
    }
}