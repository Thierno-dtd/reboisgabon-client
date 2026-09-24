package com.reboisgabon.client.dto.suivis;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Suivi {

    private String essenceNom;
    private String siteNom;
    private String id;
    private String campagne;
    private LocalDate dateControle;
    private BigDecimal tauxSurvie;
    private Integer nombrePlantsVivants;
    private String observations;
    private LocalDate prochaineDateControle;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCampagne() {
        return campagne;
    }

    public void setCampagne(String campagne) {
        this.campagne = campagne;
    }

    public LocalDate getDateControle() {
        return dateControle;
    }

    public void setDateControle(LocalDate dateControle) {
        this.dateControle = dateControle;
    }

    public BigDecimal getTauxSurvie() {
        return tauxSurvie;
    }

    public void setTauxSurvie(BigDecimal tauxSurvie) {
        this.tauxSurvie = tauxSurvie;
    }

    public Integer getNombrePlantsVivants() {
        return nombrePlantsVivants;
    }

    public void setNombrePlantsVivants(Integer nombrePlantsVivants) {
        this.nombrePlantsVivants = nombrePlantsVivants;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDate getProchaineDateControle() {
        return prochaineDateControle;
    }

    public void setProchaineDateControle(LocalDate prochaineDateControle) {
        this.prochaineDateControle = prochaineDateControle;
    }

    public String getSiteNom() {
        return siteNom;
    }

    public void setSiteNom(String siteNom) {
        this.siteNom = siteNom;
    }

    public String getEssenceNom() {
        return essenceNom;
    }

    public void setEssenceNom(String essenceNom) {
        this.essenceNom = essenceNom;
    }
}