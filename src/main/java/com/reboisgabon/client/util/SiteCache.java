package com.reboisgabon.client.util;

import com.reboisgabon.client.api.endpoints.SitesApi;
import com.reboisgabon.client.dto.sites.Site;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class SiteCache {

    private static final SiteCache INSTANCE = new SiteCache();

    private final SitesApi sitesApi = new SitesApi();
    private final Map<String, Site> parId = new ConcurrentHashMap<>();
    private volatile boolean charge = false;
    private volatile boolean enCoursDeChargement = false;

    private SiteCache() {
    }

    public static SiteCache getInstance() {
        return INSTANCE;
    }

    public void assurerCharge(Consumer<List<Site>> auChargement) {
        if (charge) {
            auChargement.accept(List.copyOf(parId.values()));
            return;
        }
        if (enCoursDeChargement) {
            return;
        }
        enCoursDeChargement = true;
        new Thread(() -> {
            try {
                var page = sitesApi.rechercher(new HashMap<>());
                page.getResults().forEach(site -> parId.put(site.getId(), site));
                charge = true;
                enCoursDeChargement = false;
                Platform.runLater(() -> auChargement.accept(List.copyOf(parId.values())));
            } catch (Exception e) {
                enCoursDeChargement = false;
            }
        }).start();
    }

    public String nomDe(String id) {
        Site site = parId.get(id);
        return site != null ? site.getNom() : id;
    }

    public void invalider() {
        charge = false;
        parId.clear();
    }
}