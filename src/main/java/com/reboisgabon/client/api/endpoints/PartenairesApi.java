package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.finances.Partenaire;
import com.reboisgabon.client.dto.finances.PartenaireRequete;
import com.reboisgabon.client.util.JsonMapper;

import java.util.Map;

public class PartenairesApi {

    public PageDrf<Partenaire> rechercher(Map<String, String> filtres) throws Exception {
        StringBuilder chemin = new StringBuilder("partenaires/?");
        filtres.forEach((cle, valeur) -> {
            if (valeur != null && !valeur.isBlank()) {
                chemin.append(cle).append("=").append(valeur).append("&");
            }
        });
        var reponse = ApiClient.getInstance().get(chemin.toString());
        return lirePage(reponse.body());
    }

    public PageDrf<Partenaire> rechercherUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return lirePage(reponse.body());
    }

    public Partenaire creer(PartenaireRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("partenaires/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Partenaire.class);
    }

    public Partenaire modifier(String id, PartenaireRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().patch("partenaires/" + id + "/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Partenaire.class);
    }

    private PageDrf<Partenaire> lirePage(String corps) throws Exception {
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, Partenaire.class);
        return JsonMapper.instance().readValue(corps, type);
    }
}