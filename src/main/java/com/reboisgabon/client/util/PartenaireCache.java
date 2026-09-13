package com.reboisgabon.client.util;

import com.reboisgabon.client.api.endpoints.PartenairesApi;
import com.reboisgabon.client.dto.finances.Partenaire;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class PartenaireCache {

    private static final PartenaireCache INSTANCE = new PartenaireCache();

    private final PartenairesApi partenairesApi = new PartenairesApi();
    private final Map<String, Partenaire> parId = new ConcurrentHashMap<>();
    private volatile boolean charge = false;
    private volatile boolean enCoursDeChargement = false;

    private PartenaireCache() {
    }

    public static PartenaireCache getInstance() {
        return INSTANCE;
    }

    public void assurerCharge(Consumer<List<Partenaire>> auChargement) {
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
                var page = partenairesApi.rechercher(new HashMap<>());
                page.getResults().forEach(partenaire -> parId.put(partenaire.getId(), partenaire));
                while (page.getNext() != null) {
                    page = partenairesApi.rechercherUrl(page.getNext());
                    page.getResults().forEach(partenaire -> parId.put(partenaire.getId(), partenaire));
                }
                charge = true;
                enCoursDeChargement = false;
                Platform.runLater(() -> auChargement.accept(List.copyOf(parId.values())));
            } catch (Exception e) {
                enCoursDeChargement = false;
            }
        }).start();
    }

    public String nomDe(String id) {
        Partenaire partenaire = parId.get(id);
        return partenaire != null ? partenaire.getNom() : id;
    }

    public void invalider() {
        charge = false;
        parId.clear();
    }
}