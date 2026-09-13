package com.reboisgabon.client.dto.campagnes;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Campagne {

    private String id;
    private String site;
    private String essence;
    private LocalDate datePlantation;
    private Integer nombrePlants;
    private String responsable;
    private BigDecimal tauxSurvieMoyen;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getEssence() {
        return essence;
    }

    public void setEssence(String essence) {
        this.essence = essence;
    }

    public LocalDate getDatePlantation() {
        return datePlantation;
    }

    public void setDatePlantation(LocalDate datePlantation) {
        this.datePlantation = datePlantation;
    }

    public Integer getNombrePlants() {
        return nombrePlants;
    }

    public void setNombrePlants(Integer nombrePlants) {
        this.nombrePlants = nombrePlants;
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