package com.reboisgabon.client.config;

public final class AppConfig {

    public static final String SERVER_BASE_URL = "http://localhost:8000";
    public static final String API_BASE_URL = SERVER_BASE_URL + "/api/";
    public static final int TIMEOUT_CONNEXION_SECONDES = 10;
    public static final int TIMEOUT_REQUETE_SECONDES = 20;

    private AppConfig() {
    }
}