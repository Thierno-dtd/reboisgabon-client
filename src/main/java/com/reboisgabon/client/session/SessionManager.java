package com.reboisgabon.client.session;

import com.reboisgabon.client.dto.auth.PermissionsReponse;
import com.reboisgabon.client.dto.auth.UtilisateurConnecte;

public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String accessToken;
    private String refreshToken;
    private UtilisateurConnecte utilisateurConnecte;
    private PermissionsReponse permissions;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UtilisateurConnecte getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public void setUtilisateurConnecte(UtilisateurConnecte utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
    }

    public PermissionsReponse getPermissions() {
        return permissions;
    }

    public void setPermissions(PermissionsReponse permissions) {
        this.permissions = permissions;
    }

    public boolean estAuthentifie() {
        return accessToken != null && !accessToken.isBlank();
    }

    public boolean peutAcceder(String domaine, String action) {
        return permissions != null && permissions.peutAcceder(domaine, action);
    }

    public Role getRole() {
        return utilisateurConnecte != null ? utilisateurConnecte.getRole() : null;
    }

    public void vider() {
        accessToken = null;
        refreshToken = null;
        utilisateurConnecte = null;
        permissions = null;
    }
}