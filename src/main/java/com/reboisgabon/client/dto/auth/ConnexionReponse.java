package com.reboisgabon.client.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnexionReponse {

    @JsonProperty("requires_2fa")
    private boolean requiresTwoFa;

    private String access;
    private String refresh;
    private String tempToken;

    public boolean isRequiresTwoFa() {
        return requiresTwoFa;
    }

    public void setRequiresTwoFa(boolean requiresTwoFa) {
        this.requiresTwoFa = requiresTwoFa;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public String getTempToken() {
        return tempToken;
    }

    public void setTempToken(String tempToken) {
        this.tempToken = tempToken;
    }
}