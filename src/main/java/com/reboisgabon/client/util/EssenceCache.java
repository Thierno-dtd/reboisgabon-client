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
    private final java.util.List<Consumer<List<Essence>>> enAttente = new java.util.concurrent.CopyOnWriteArrayList<>();

    private EssenceCache() {
    }

    public static EssenceCache getInstance() {
        return INSTANCE;
    }

    public void assurerCharge(Consumer<List<Essence>> auChargement) {
        if (charge) {
            List<Essence> liste = new java.util.ArrayList<>(parId.values());
            liste.sort(java.util.Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER));
            auChargement.accept(List.copyOf(liste));
            return;
        }
        enAttente.add(auChargement);
        if (enCoursDeChargement) {
            return;
        }
        enCoursDeChargement = true;
        new Thread(() -> {
            try {
                var page = essencesApi.lister();
                page.getResults().forEach(essence -> parId.put(essence.getId(), essence));
                while (page.getNext() != null) {
                    page = essencesApi.listerUrl(page.getNext());
                    page.getResults().forEach(essence -> parId.put(essence.getId(), essence));
                }
                charge = true;
                enCoursDeChargement = false;
                List<Essence> liste = new java.util.ArrayList<>(parId.values());
                liste.sort(java.util.Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER));
                List<Consumer<List<Essence>>> abonnes = new java.util.ArrayList<>(enAttente);
                enAttente.clear();
                Platform.runLater(() -> abonnes.forEach(a -> a.accept(List.copyOf(liste))));
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