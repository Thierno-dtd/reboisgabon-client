package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.campagnes.Campagne;
import com.reboisgabon.client.dto.campagnes.CampagneRequete;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.util.JsonMapper;

import java.util.Map;

public class CampagnesApi {

    public PageDrf<Campagne> rechercher(Map<String, String> filtres) throws Exception {
        StringBuilder chemin = new StringBuilder("campagnes/?");
        filtres.forEach((cle, valeur) -> {
            if (valeur != null && !valeur.isBlank()) {
                chemin.append(cle).append("=").append(java.net.URLEncoder.encode(valeur.trim(), java.nio.charset.StandardCharsets.UTF_8)).append("&");
            }
        });
        var reponse = ApiClient.getInstance().get(chemin.toString());
        return lirePage(reponse.body());
    }

    public PageDrf<Campagne> rechercherUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return lirePage(reponse.body());
    }

    public Campagne creer(CampagneRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("campagnes/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Campagne.class);
    }

    public Campagne modifier(String id, CampagneRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().patch("campagnes/" + id + "/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Campagne.class);
    }

    public void supprimer(String id) throws Exception {
        ApiClient.getInstance().delete("campagnes/" + id + "/?confirm=true");
    }

    private PageDrf<Campagne> lirePage(String corps) throws Exception {
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, Campagne.class);
        return JsonMapper.instance().readValue(corps, type);
    }
}