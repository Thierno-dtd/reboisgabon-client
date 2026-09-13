package com.reboisgabon.client.util;

import com.reboisgabon.client.api.endpoints.CampagnesApi;
import com.reboisgabon.client.dto.campagnes.Campagne;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class CampagneCache {

    private static final CampagneCache INSTANCE = new CampagneCache();

    private final CampagnesApi campagnesApi = new CampagnesApi();
    private final Map<String, Campagne> parId = new ConcurrentHashMap<>();
    private volatile boolean charge = false;
    private volatile boolean enCoursDeChargement = false;

    private CampagneCache() {
    }

    public static CampagneCache getInstance() {
        return INSTANCE;
    }

    public void assurerCharge(Consumer<List<Campagne>> auChargement) {
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
                var page = campagnesApi.rechercher(new HashMap<>());
                page.getResults().forEach(campagne -> parId.put(campagne.getId(), campagne));
                while (page.getNext() != null) {
                    page = campagnesApi.rechercherUrl(page.getNext());
                    page.getResults().forEach(campagne -> parId.put(campagne.getId(), campagne));
                }
                charge = true;
                enCoursDeChargement = false;
                Platform.runLater(() -> auChargement.accept(List.copyOf(parId.values())));
            } catch (Exception e) {
                enCoursDeChargement = false;
            }
        }).start();
    }

    public String libelleDe(String id) {
        Campagne campagne = parId.get(id);
        if (campagne == null) {
            return id;
        }
        return SiteCache.getInstance().nomDe(campagne.getSite()) + " — " + EssenceCache.getInstance().nomDe(campagne.getEssence())
                + (campagne.getDatePlantation() != null ? " (" + campagne.getDatePlantation() + ")" : "");
    }
}