package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurSimple;
import com.reboisgabon.client.util.JsonMapper;

public class UsersApi {

    public PageDrf<UtilisateurSimple> lister() throws Exception {
        var reponse = ApiClient.getInstance().get("users/");
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, UtilisateurSimple.class);
        return JsonMapper.instance().readValue(reponse.body(), type);
    }
}