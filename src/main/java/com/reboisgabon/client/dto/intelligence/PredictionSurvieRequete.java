package com.reboisgabon.client.dto.intelligence;

import java.math.BigDecimal;

public class PredictionSurvieRequete {

    private String essence;
    private String province;
    private BigDecimal superficieSite;
    private Integer nombrePlants;
    private Integer moisPlantation;
    private boolean croissanceRapide;

    public String getEssence() {
        return essence;
    }

    public void setEssence(String essence) {
        this.essence = essence;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public BigDecimal getSuperficieSite() {
        return superficieSite;
    }

    public void setSuperficieSite(BigDecimal superficieSite) {
        this.superficieSite = superficieSite;
    }

    public Integer getNombrePlants() {
        return nombrePlants;
    }

    public void setNombrePlants(Integer nombrePlants) {
        this.nombrePlants = nombrePlants;
    }

    public Integer getMoisPlantation() {
        return moisPlantation;
    }

    public void setMoisPlantation(Integer moisPlantation) {
        this.moisPlantation = moisPlantation;
    }

    public boolean isCroissanceRapide() {
        return croissanceRapide;
    }

    public void setCroissanceRapide(boolean croissanceRapide) {
        this.croissanceRapide = croissanceRapide;
    }
}