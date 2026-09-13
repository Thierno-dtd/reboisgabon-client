package com.reboisgabon.client.dto.utilisateurs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reboisgabon.client.session.Role;

public class UtilisateurSimple {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    @JsonProperty("is_active")
    private boolean actif;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + email + ")";
    }
}