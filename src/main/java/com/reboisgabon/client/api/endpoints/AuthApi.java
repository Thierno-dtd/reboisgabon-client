package com.reboisgabon.client.api.endpoints;

import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.auth.ConnexionReponse;
import com.reboisgabon.client.dto.auth.ConnexionRequete;
import com.reboisgabon.client.dto.auth.DemandeReinitialisationRequete;
import com.reboisgabon.client.dto.auth.PermissionsReponse;
import com.reboisgabon.client.dto.auth.ReinitialisationMotDePasseRequete;
import com.reboisgabon.client.dto.auth.TokensReponse;
import com.reboisgabon.client.dto.auth.UtilisateurConnecte;
import com.reboisgabon.client.dto.auth.VerificationOtpRequete;
import com.reboisgabon.client.util.JsonMapper;

import java.net.http.HttpResponse;

public class AuthApi {

    public ConnexionReponse connexion(String email, String motDePasse) throws Exception {
        ConnexionRequete requete = new ConnexionRequete();
        requete.setEmail(email);
        requete.setPassword(motDePasse);
        HttpResponse<String> reponse = ApiClient.getInstance().post("auth/login/", requete);
        return JsonMapper.instance().readValue(reponse.body(), ConnexionReponse.class);
    }

    public TokensReponse verifierOtp(String tempToken, String otpCode) throws Exception {
        VerificationOtpRequete requete = new VerificationOtpRequete();
        requete.setTempToken(tempToken);
        requete.setOtpCode(otpCode);
        HttpResponse<String> reponse = ApiClient.getInstance().post("auth/login/2fa/verify/", requete);
        return JsonMapper.instance().readValue(reponse.body(), TokensReponse.class);
    }

    public UtilisateurConnecte recupererProfil() throws Exception {
        HttpResponse<String> reponse = ApiClient.getInstance().get("auth/me/");
        return JsonMapper.instance().readValue(reponse.body(), UtilisateurConnecte.class);
    }

    public PermissionsReponse recupererPermissions() throws Exception {
        HttpResponse<String> reponse = ApiClient.getInstance().get("auth/mes-permissions/");
        return JsonMapper.instance().readValue(reponse.body(), PermissionsReponse.class);
    }

    public void demanderReinitialisation(String email) throws Exception {
        DemandeReinitialisationRequete requete = new DemandeReinitialisationRequete();
        requete.setEmail(email);
        ApiClient.getInstance().post("auth/password/forgot/", requete);
    }

    public void reinitialiserMotDePasse(String token, String nouveauMotDePasse) throws Exception {
        ReinitialisationMotDePasseRequete requete = new ReinitialisationMotDePasseRequete();
        requete.setToken(token);
        requete.setNewPassword(nouveauMotDePasse);
        ApiClient.getInstance().post("auth/password/reset/", requete);
    }
}