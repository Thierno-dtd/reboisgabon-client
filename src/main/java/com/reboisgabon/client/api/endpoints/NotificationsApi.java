package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.notifications.Notification;
import com.reboisgabon.client.util.JsonMapper;

import java.util.HashMap;

public class NotificationsApi {

    public PageDrf<Notification> lister() throws Exception {
        var reponse = ApiClient.getInstance().get("notifications/");
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, Notification.class);
        return JsonMapper.instance().readValue(reponse.body(), type);
    }

    public int compterNonLues() throws Exception {
        var reponse = ApiClient.getInstance().get("notifications/non_lues/");
        var noeud = JsonMapper.instance().readTree(reponse.body());
        return noeud.path("nombre_non_lues").asInt(0);
    }

    public void marquerLue(String id) throws Exception {
        ApiClient.getInstance().post("notifications/" + id + "/marquer_lue/", new HashMap<>());
    }

    public void marquerToutesLues() throws Exception {
        ApiClient.getInstance().post("notifications/marquer-toutes-lues/", new HashMap<>());
    }
}