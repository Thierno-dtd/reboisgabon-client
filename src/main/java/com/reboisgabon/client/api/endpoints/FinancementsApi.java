package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.finances.Financement;
import com.reboisgabon.client.dto.finances.FinancementRequete;
import com.reboisgabon.client.util.JsonMapper;

import java.util.Map;

public class FinancementsApi {

    public PageDrf<Financement> rechercher(Map<String, String> filtres) throws Exception {
        StringBuilder chemin = new StringBuilder("financements/?");
        filtres.forEach((cle, valeur) -> {
            if (valeur != null && !valeur.isBlank()) {
                chemin.append(cle).append("=").append(valeur).append("&");
            }
        });
        var reponse = ApiClient.getInstance().get(chemin.toString());
        return lirePage(reponse.body());
    }

    public PageDrf<Financement> rechercherUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return lirePage(reponse.body());
    }

    public Financement creer(FinancementRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("financements/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Financement.class);
    }

    private PageDrf<Financement> lirePage(String corps) throws Exception {
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, Financement.class);
        return JsonMapper.instance().readValue(corps, type);
    }
}