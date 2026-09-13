package com.reboisgabon.client.dto.finances;

import java.math.BigDecimal;

public class BudgetCampagneRequete {

    private String campagne;
    private BigDecimal montantAlloue;
    private BigDecimal montantReel;

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