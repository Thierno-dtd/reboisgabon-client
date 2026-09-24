package com.reboisgabon.client.dto.notifications;

import java.time.OffsetDateTime;

public class Notification {

    private String id;
    private String titre;
    private String message;
    private String typeNotification;
    private boolean lue;
    private String lienObjetId;
    private String lienModele;
    private OffsetDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTypeNotification() {
        return typeNotification;
    }

    public void setTypeNotification(String typeNotification) {
        this.typeNotification = typeNotification;
    }

    public boolean isLue() {
        return lue;
    }

    public void setLue(boolean lue) {
        this.lue = lue;
    }

    public String getLienObjetId() {
        return lienObjetId;
    }

    public void setLienObjetId(String lienObjetId) {
        this.lienObjetId = lienObjetId;
    }

    public String getLienModele() {
        return lienModele;
    }

    public void setLienModele(String lienModele) {
        this.lienModele = lienModele;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
