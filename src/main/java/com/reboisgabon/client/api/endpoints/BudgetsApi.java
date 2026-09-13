package com.reboisgabon.client.api.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.reboisgabon.client.api.ApiClient;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.finances.BudgetCampagne;
import com.reboisgabon.client.dto.finances.BudgetCampagneRequete;
import com.reboisgabon.client.util.JsonMapper;

public class BudgetsApi {

    public PageDrf<BudgetCampagne> rechercher(String campagneId) throws Exception {
        String chemin = campagneId == null || campagneId.isBlank()
                ? "budgets-campagne/"
                : "budgets-campagne/?campagne=" + campagneId;
        var reponse = ApiClient.getInstance().get(chemin);
        return lirePage(reponse.body());
    }

    public PageDrf<BudgetCampagne> rechercherUrl(String urlAbsolue) throws Exception {
        var reponse = ApiClient.getInstance().getUrlAbsolue(urlAbsolue);
        return lirePage(reponse.body());
    }

    public BudgetCampagne creer(BudgetCampagneRequete requete) throws Exception {
        var reponse = ApiClient.getInstance().post("budgets-campagne/", requete);
        return JsonMapper.instance().readValue(reponse.body(), BudgetCampagne.class);
    }

    private PageDrf<BudgetCampagne> lirePage(String corps) throws Exception {
        JavaType type = JsonMapper.instance().getTypeFactory().constructParametricType(PageDrf.class, BudgetCampagne.class);
        return JsonMapper.instance().readValue(corps, type);
    }
}