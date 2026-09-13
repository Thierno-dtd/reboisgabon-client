package com.reboisgabon.client.dto.finances;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Financement {

    private String id;
    private String partenaire;
    private String campagne;
    private String site;
    private Devise devise;
    private BigDecimal montant;
    private LocalDate dateFinancement;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPartenaire() {
        return partenaire;
    }

    public void setPartenaire(String partenaire) {
        this.partenaire = partenaire;
    }

    public String getCampagne() {
        return campagne;
    }

    public void setCampagne(String campagne) {
        this.campagne = campagne;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Devise getDevise() {
        return devise;
    }

    public void setDevise(Devise devise) {
        this.devise = devise;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDate getDateFinancement() {
        return dateFinancement;
    }

    public void setDateFinancement(LocalDate dateFinancement) {
        this.dateFinancement = dateFinancement;
    }
}