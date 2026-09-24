package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.objectifs.Objectif;
import com.reboisgabon.client.dto.objectifs.ObjectifRequete;
import com.reboisgabon.client.util.JsonMapper;

import java.util.Map;

public class ObjectifsApi {

    public PageDrf<Objectif> rechercher(Map<String, String> filtres) throws Exception {
        StringBuilder chemin = new StringBuilder("objectifs/?");
        filtres.forEach((cle, valeur) -> {
            if (valeur != null && !valeur.isBlank()) {
                chemin.append(cle).append("=").append(java.net.URLEncoder.encode(valeur.trim(), java.nio.charset.StandardCharsets.UTF_8)).append("&");
            }
        });
        var reponse = ApiClient.getInstance().get(chemin.toString());
        return lirePage(reponse.body());
    }

    public PageDrf<Objectif> rechercherUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return lirePage(reponse.body());
    }

    public Objectif creer(ObjectifRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("objectifs/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Objectif.class);
    }

    public Objectif modifier(String id, ObjectifRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().patch("objectifs/" + id + "/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Objectif.class);
    }

    public void supprimer(String id) throws Exception {
        ApiClient.getInstance().delete("objectifs/" + id + "/?confirm=true");
    }

    private PageDrf<Objectif> lirePage(String corps) throws Exception {
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, Objectif.class);
        return JsonMapper.instance().readValue(corps, type);
    }
}