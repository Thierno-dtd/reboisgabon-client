package com.reboisgabon.client.dto.auth;

import com.reboisgabon.client.session.Role;

import java.util.List;
import java.util.Map;

public class PermissionsReponse {

    private Role role;
    private boolean estAdministrateur;
    private Map<String, List<String>> permissions;
    private Map<String, Map<String, List<String>>> matriceComplete;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEstAdministrateur() {
        return estAdministrateur;
    }

    public void setEstAdministrateur(boolean estAdministrateur) {
        this.estAdministrateur = estAdministrateur;
    }

    public Map<String, List<String>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, List<String>> permissions) {
        this.permissions = permissions;
    }

    public Map<String, Map<String, List<String>>> getMatriceComplete() {
        return matriceComplete;
    }

    public void setMatriceComplete(Map<String, Map<String, List<String>>> matriceComplete) {
        this.matriceComplete = matriceComplete;
    }

    public boolean peutAcceder(String domaine, String action) {
        if (estAdministrateur) {
            return true;
        }
        if (permissions == null || !permissions.containsKey(domaine)) {
            return false;
        }
        return permissions.get(domaine).contains(action);
    }
}