package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurCreationRequete;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurModificationRequete;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurSimple;
import com.reboisgabon.client.util.JsonMapper;

import java.util.HashMap;
import java.util.Map;

public class UsersApi {

    public PageDrf<UtilisateurSimple> lister() throws Exception {
        return lister(new HashMap<>());
    }

    public PageDrf<UtilisateurSimple> lister(Map<String, String> filtres) throws Exception {
        StringBuilder chemin = new StringBuilder("users/?");
        filtres.forEach((cle, valeur) -> {
            if (valeur != null && !valeur.isBlank()) {
                chemin.append(cle).append("=").append(valeur).append("&");
            }
        });
        var reponse = ApiClient.getInstance().get(chemin.toString());
        return lirePage(reponse.body());
    }

    public PageDrf<UtilisateurSimple> listerUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return lirePage(reponse.body());
    }

    public UtilisateurSimple creer(UtilisateurCreationRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("users/", requete);
        return JsonMapper.instance().readValue(reponse.body(), UtilisateurSimple.class);
    }

    public UtilisateurSimple modifier(String id, UtilisateurModificationRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().patch("users/" + id + "/", requete);
        return JsonMapper.instance().readValue(reponse.body(), UtilisateurSimple.class);
    }

    public void supprimer(String id) throws Exception {
        ApiClient.getInstance().delete("users/" + id + "/?confirm=true");
    }

    public void reactiver(String id) throws Exception {
        ApiClient.getInstance().post("users/" + id + "/reactivate/", new HashMap<>());
    }

    private PageDrf<UtilisateurSimple> lirePage(String corps) throws Exception {
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, UtilisateurSimple.class);
        return JsonMapper.instance().readValue(corps, type);
    }
}