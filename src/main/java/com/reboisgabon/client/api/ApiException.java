package com.reboisgabon.client.api;

public class ApiException extends RuntimeException {

    private final int statutHttp;
    private final String corpsReponse;

    public ApiException(int statutHttp, String corpsReponse) {
        super("Erreur API — statut " + statutHttp);
        this.statutHttp = statutHttp;
        this.corpsReponse = corpsReponse;
    }

    public int getStatutHttp() {
        return statutHttp;
    }

    public String getCorpsReponse() {
        return corpsReponse;
    }
}