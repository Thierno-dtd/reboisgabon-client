package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.suivis.Suivi;
import com.reboisgabon.client.dto.suivis.SuiviRequete;
import com.reboisgabon.client.util.JsonMapper;

import java.util.Map;

public class SuivisApi {

    public PageDrf<Suivi> rechercher(Map<String, String> filtres) throws Exception {
        StringBuilder chemin = new StringBuilder("suivis/?");
        filtres.forEach((cle, valeur) -> {
            if (valeur != null && !valeur.isBlank()) {
                chemin.append(cle).append("=").append(java.net.URLEncoder.encode(valeur.trim(), java.nio.charset.StandardCharsets.UTF_8)).append("&");
            }
        });
        var reponse = ApiClient.getInstance().get(chemin.toString());
        return lirePage(reponse.body());
    }

    public PageDrf<Suivi> rechercherUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return lirePage(reponse.body());
    }

    public Suivi creer(SuiviRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("suivis/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Suivi.class);
    }

    public Suivi modifier(String id, SuiviRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().patch("suivis/" + id + "/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Suivi.class);
    }

    public void supprimer(String id) throws Exception {
        ApiClient.getInstance().delete("suivis/" + id + "/?confirm=true");
    }

    private PageDrf<Suivi> lirePage(String corps) throws Exception {
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, Suivi.class);
        return JsonMapper.instance().readValue(corps, type);
    }
}