package com.reboisgabon.client.dto.utilisateurs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reboisgabon.client.session.Role;

public class UtilisateurModificationRequete {

    private String firstName;
    private String lastName;
    private Role role;

    @JsonProperty("is_active")
    private Boolean actif;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}