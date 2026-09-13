package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.suivis.PhotoSuivi;
import com.reboisgabon.client.util.JsonMapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PhotosSuiviApi {

    public PhotoSuivi uploader(String suiviId, File fichier, String legende) throws Exception {
        Map<String, String> champs = new HashMap<>();
        champs.put("suivi", suiviId);
        if (legende != null && !legende.isBlank()) {
            champs.put("legende", legende);
        }
        var reponse = ApiClient.getInstance().postMultipart("photos-suivi/", champs, "image", fichier);
        return JsonMapper.instance().readValue(reponse.body(), PhotoSuivi.class);
    }

    public PageDrf<PhotoSuivi> lister(String suiviId) throws Exception {
        var reponse = ApiClient.getInstance().get("photos-suivi/?suivi=" + suiviId);
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, PhotoSuivi.class);
        return JsonMapper.instance().readValue(reponse.body(), type);
    }
}