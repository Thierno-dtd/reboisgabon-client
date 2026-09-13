package com.reboisgabon.client.dto.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public class Notification {

    private String id;
    private String message;

    @JsonProperty("est_lue")
    private boolean lue;

    private ZonedDateTime dateCreation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isLue() {
        return lue;
    }

    public void setLue(boolean lue) {
        this.lue = lue;
    }

    public ZonedDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(ZonedDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}