package com.reboisgabon.client.dto.sites;

import java.math.BigDecimal;

public class Site {

    private String id;
    private String nom;
    private String localite;
    private String province;
    private BigDecimal superficieHectares;
    private StatutSite statut;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String responsable;
    private BigDecimal tauxSurvieMoyen;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getLocalite() {
        return localite;
    }

    public void setLocalite(String localite) {
        this.localite = localite;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public BigDecimal getSuperficieHectares() {
        return superficieHectares;
    }

    public void setSuperficieHectares(BigDecimal superficieHectares) {
        this.superficieHectares = superficieHectares;
    }

    public StatutSite getStatut() {
        return statut;
    }

    public void setStatut(StatutSite statut) {
        this.statut = statut;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public BigDecimal getTauxSurvieMoyen() {
        return tauxSurvieMoyen;
    }

    public void setTauxSurvieMoyen(BigDecimal tauxSurvieMoyen) {
        this.tauxSurvieMoyen = tauxSurvieMoyen;
    }
}