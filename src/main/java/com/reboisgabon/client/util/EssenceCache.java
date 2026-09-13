package com.reboisgabon.client.util;

import com.reboisgabon.client.api.endpoints.EssencesApi;
import com.reboisgabon.client.dto.essences.Essence;
import javafx.application.Platform;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class EssenceCache {

    private static final EssenceCache INSTANCE = new EssenceCache();

    private final EssencesApi essencesApi = new EssencesApi();
    private final Map<String, Essence> parId = new ConcurrentHashMap<>();
    private volatile boolean charge = false;
    private volatile boolean enCoursDeChargement = false;

    private EssenceCache() {
    }

    public static EssenceCache getInstance() {
        return INSTANCE;
    }

    public void assurerCharge(Consumer<List<Essence>> auChargement) {
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
                var page = essencesApi.lister();
                page.getResults().forEach(essence -> parId.put(essence.getId(), essence));
                charge = true;
                enCoursDeChargement = false;
                Platform.runLater(() -> auChargement.accept(List.copyOf(parId.values())));
            } catch (Exception e) {
                enCoursDeChargement = false;
            }
        }).start();
    }

    public String nomDe(String id) {
        Essence essence = parId.get(id);
        return essence != null ? essence.getNom() : id;
    }

    public void invalider() {
        charge = false;
        parId.clear();
    }
}