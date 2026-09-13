package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.essences.Essence;
import com.reboisgabon.client.dto.essences.EssenceRequete;
import com.reboisgabon.client.util.JsonMapper;

public class EssencesApi {

    public PageDrf<Essence> lister() throws Exception {
        var reponse = ApiClient.getInstance().get("essences/");
        return lirePage(reponse.body());
    }

    public PageDrf<Essence> listerUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return lirePage(reponse.body());
    }

    public PageDrf<Essence> rechercher(String recherche) throws Exception {
        var reponse = ApiClient.getInstance().get("essences/?search=" + recherche);
        return lirePage(reponse.body());
    }

    public Essence creer(EssenceRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("essences/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Essence.class);
    }

    public Essence modifier(String id, EssenceRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().patch("essences/" + id + "/", requete);
        return JsonMapper.instance().readValue(reponse.body(), Essence.class);
    }

    public void supprimer(String id) throws Exception {
        ApiClient.getInstance().delete("essences/" + id + "/?confirm=true");
    }

    private PageDrf<Essence> lirePage(String corps) throws Exception {
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, Essence.class);
        return JsonMapper.instance().readValue(corps, type);
    }
}