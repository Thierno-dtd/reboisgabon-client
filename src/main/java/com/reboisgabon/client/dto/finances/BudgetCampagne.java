package com.reboisgabon.client.dto.finances;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class BudgetCampagne {

    private String id;
    private String campagne;
    @JsonProperty("montant_alloue")
    private BigDecimal montantAlloue;
    @JsonProperty("montant_reel")
    private BigDecimal montantReel;

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

    public BigDecimal getMontantAlloue() {
        return montantAlloue;
    }

    public void setMontantAlloue(BigDecimal montantAlloue) {
        this.montantAlloue = montantAlloue;
    }

    public BigDecimal getMontantReel() {
        return montantReel;
    }

    public void setMontantReel(BigDecimal montantReel) {
        this.montantReel = montantReel;
    }
}