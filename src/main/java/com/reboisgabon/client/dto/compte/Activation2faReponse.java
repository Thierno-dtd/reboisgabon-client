package com.reboisgabon.client.dto.compte;

public class Activation2faReponse {

    private String secret;
    private String provisioningUri;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getProvisioningUri() {
        return provisioningUri;
    }

    public void setProvisioningUri(String provisioningUri) {
        this.provisioningUri = provisioningUri;
    }
}