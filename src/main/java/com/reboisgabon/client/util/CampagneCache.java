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
    private final java.util.List<Consumer<List<Campagne>>> enAttente = new java.util.concurrent.CopyOnWriteArrayList<>();

    private CampagneCache() {
    }

    public static CampagneCache getInstance() {
        return INSTANCE;
    }

    public void assurerCharge(Consumer<List<Campagne>> auChargement) {
        if (charge) {
            List<Campagne> liste = new java.util.ArrayList<>(parId.values());
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
                var page = campagnesApi.rechercher(new HashMap<>());
                page.getResults().forEach(campagne -> parId.put(campagne.getId(), campagne));
                while (page.getNext() != null) {
                    page = campagnesApi.rechercherUrl(page.getNext());
                    page.getResults().forEach(campagne -> parId.put(campagne.getId(), campagne));
                }
                charge = true;
                enCoursDeChargement = false;
                List<Campagne> liste = new java.util.ArrayList<>(parId.values());
                liste.sort(java.util.Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER));
                List<Consumer<List<Campagne>>> abonnes = new java.util.ArrayList<>(enAttente);
                enAttente.clear();
                Platform.runLater(() -> abonnes.forEach(a -> a.accept(List.copyOf(liste))));
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
        String site = campagne.getSiteNom() != null ? campagne.getSiteNom() : SiteCache.getInstance().nomDe(campagne.getSite());
        String essence = campagne.getEssenceNom() != null ? campagne.getEssenceNom() : EssenceCache.getInstance().nomDe(campagne.getEssence());
        return site + " — " + essence
                + (campagne.getDatePlantation() != null ? " (" + campagne.getDatePlantation() + ")" : "");
    }
}