package com.reboisgabon.client.dto.objectifs;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ObjectifRequete {

    private String titre;
    private String description;
    private Portee portee;
    private String province;
    private String site;
    private Integer nombrePlantsCible;
    private BigDecimal tauxSurvieMinimumVise;
    private LocalDate dateDebut;
    private LocalDate dateEcheance;
    private String responsable;

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Portee getPortee() {
        return portee;
    }

    public void setPortee(Portee portee) {
        this.portee = portee;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Integer getNombrePlantsCible() {
        return nombrePlantsCible;
    }

    public void setNombrePlantsCible(Integer nombrePlantsCible) {
        this.nombrePlantsCible = nombrePlantsCible;
    }

    public BigDecimal getTauxSurvieMinimumVise() {
        return tauxSurvieMinimumVise;
    }

    public void setTauxSurvieMinimumVise(BigDecimal tauxSurvieMinimumVise) {
        this.tauxSurvieMinimumVise = tauxSurvieMinimumVise;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }
}