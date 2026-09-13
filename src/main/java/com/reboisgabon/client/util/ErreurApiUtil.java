package com.reboisgabon.client.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.ApiException;

import java.util.Iterator;
import java.util.Map;

public final class ErreurApiUtil {

    private ErreurApiUtil() {
    }

    public static String message(ApiException exception) {
        try {
            JsonNode racine = JsonMapper.instance().readTree(exception.getCorpsReponse());
            if (racine.has("detail")) {
                return racine.get("detail").asText();
            }
            StringBuilder message = new StringBuilder();
            Iterator<Map.Entry<String, JsonNode>> champs = racine.fields();
            while (champs.hasNext()) {
                Map.Entry<String, JsonNode> entree = champs.next();
                JsonNode valeurs = entree.getValue();
                if (valeurs.isArray()) {
                    for (JsonNode valeur : valeurs) {
                        message.append(entree.getKey()).append(" : ").append(valeur.asText()).append("\n");
                    }
                } else {
                    message.append(entree.getKey()).append(" : ").append(valeurs.asText()).append("\n");
                }
            }
            if (message.isEmpty()) {
                return "Erreur inconnue (statut " + exception.getStatutHttp() + ").";
            }
            return message.toString().trim();
        } catch (Exception e) {
            return "Erreur serveur (statut " + exception.getStatutHttp() + ").";
        }
    }
}