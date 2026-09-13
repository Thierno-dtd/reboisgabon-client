package com.reboisgabon.client.dto.intelligence;

import java.math.BigDecimal;

public class PredictionSurvieReponse {

    private BigDecimal tauxSurviePredit;
    private String niveauConfiance;
    private boolean campagneARisque;

    public BigDecimal getTauxSurviePredit() {
        return tauxSurviePredit;
    }

    public void setTauxSurviePredit(BigDecimal tauxSurviePredit) {
        this.tauxSurviePredit = tauxSurviePredit;
    }

    public String getNiveauConfiance() {
        return niveauConfiance;
    }

    public void setNiveauConfiance(String niveauConfiance) {
        this.niveauConfiance = niveauConfiance;
    }

    public boolean isCampagneARisque() {
        return campagneARisque;
    }

    public void setCampagneARisque(boolean campagneARisque) {
        this.campagneARisque = campagneARisque;
    }
}