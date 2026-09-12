package com.reboisgabon.client.api;

import com.reboisgabon.client.config.AppConfig;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient httpClient;
    private SessionExpirationListener ecouteurExpiration;

    public interface SessionExpirationListener {
        void onSessionExpiree();
    }

    private ApiClient() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(AppConfig.TIMEOUT_CONNEXION_SECONDES))
                .build();
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public void definirEcouteurExpiration(SessionExpirationListener ecouteur) {
        this.ecouteurExpiration = ecouteur;
    }

    public HttpResponse<String> get(String chemin) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + chemin))
                .timeout(Duration.ofSeconds(AppConfig.TIMEOUT_REQUETE_SECONDES))
                .GET();
        return executer(builder);
    }

    public HttpResponse<String> post(String chemin, Object corps) {
        return envoyerAvecCorps(chemin, corps, "POST");
    }

    public HttpResponse<String> patch(String chemin, Object corps) {
        return envoyerAvecCorps(chemin, corps, "PATCH");
    }

    public HttpResponse<String> delete(String chemin) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + chemin))
                .timeout(Duration.ofSeconds(AppConfig.TIMEOUT_REQUETE_SECONDES))
                .DELETE();
        return executer(builder);
    }

    public HttpResponse<byte[]> telechargerBinaire(String chemin) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.API_BASE_URL + chemin))
                    .timeout(Duration.ofSeconds(AppConfig.TIMEOUT_REQUETE_SECONDES))
                    .GET();
            ajouterAutorisation(builder);
            HttpResponse<byte[]> reponse = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (reponse.statusCode() >= 400) {
                throw new ApiException(reponse.statusCode(), new String(reponse.body()));
            }
            return reponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> envoyerAvecCorps(String chemin, Object corps, String methode) {
        try {
            String json = JsonMapper.instance().writeValueAsString(corps);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.API_BASE_URL + chemin))
                    .timeout(Duration.ofSeconds(AppConfig.TIMEOUT_REQUETE_SECONDES))
                    .header("Content-Type", "application/json")
                    .method(methode, HttpRequest.BodyPublishers.ofString(json));
            return executer(builder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> executer(HttpRequest.Builder builder) {
        try {
            ajouterAutorisation(builder);
            HttpResponse<String> reponse = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (reponse.statusCode() == 401 && SessionManager.getInstance().estAuthentifie()) {
                if (tenterRafraichissement()) {
                    ajouterAutorisation(builder);
                    reponse = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                } else {
                    SessionManager.getInstance().vider();
                    if (ecouteurExpiration != null) {
                        ecouteurExpiration.onSessionExpiree();
                    }
                    throw new ApiException(401, reponse.body());
                }
            }
            if (reponse.statusCode() >= 400) {
                throw new ApiException(reponse.statusCode(), reponse.body());
            }
            return reponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void ajouterAutorisation(HttpRequest.Builder builder) {
        String token = SessionManager.getInstance().getAccessToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
    }

    private boolean tenterRafraichissement() {
        String refresh = SessionManager.getInstance().getRefreshToken();
        if (refresh == null || refresh.isBlank()) {
            return false;
        }
        try {
            String corps = JsonMapper.instance().writeValueAsString(Map.of("refresh", refresh));
            HttpRequest requete = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.API_BASE_URL + "auth/token/refresh/"))
                    .timeout(Duration.ofSeconds(AppConfig.TIMEOUT_REQUETE_SECONDES))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(corps))
                    .build();
            HttpResponse<String> reponse = httpClient.send(requete, HttpResponse.BodyHandlers.ofString());
            if (reponse.statusCode() != 200) {
                return false;
            }
            Map<?, ?> resultat = JsonMapper.instance().readValue(reponse.body(), Map.class);
            Object nouvelAccess = resultat.get("access");
            if (nouvelAccess == null) {
                return false;
            }
            SessionManager.getInstance().setAccessToken(nouvelAccess.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}