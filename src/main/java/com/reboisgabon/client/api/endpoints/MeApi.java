package com.reboisgabon.client.api.endpoints;

import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.compte.Activation2faReponse;
import com.reboisgabon.client.dto.compte.ChangementMotDePasseRequete;
import com.reboisgabon.client.dto.compte.CodeOtpRequete;
import com.reboisgabon.client.dto.compte.ProfilModificationRequete;
import com.reboisgabon.client.util.JsonMapper;

import java.util.HashMap;

public class MeApi {

    public void modifierProfil(ProfilModificationRequete requete) throws Exception {
        ApiClient.getInstance().patch("me/update/", requete);
    }

    public void changerMotDePasse(String ancien, String nouveau) throws Exception {
        ChangementMotDePasseRequete requete = new ChangementMotDePasseRequete();
        requete.setOldPassword(ancien);
        requete.setNewPassword(nouveau);
        ApiClient.getInstance().post("me/change-password/", requete);
    }

    public Activation2faReponse initialiser2fa() throws Exception {
        var reponse = ApiClient.getInstance().post("auth/2fa/setup/init/", new HashMap<>());
        return JsonMapper.instance().readValue(reponse.body(), Activation2faReponse.class);
    }

    public void confirmer2fa(String otpCode) throws Exception {
        CodeOtpRequete requete = new CodeOtpRequete();
        requete.setOtpCode(otpCode);
        ApiClient.getInstance().post("auth/2fa/setup/confirm/", requete);
    }

    public void desactiver2fa() throws Exception {
        ApiClient.getInstance().post("auth/2fa/disable/", new HashMap<>());
    }
}