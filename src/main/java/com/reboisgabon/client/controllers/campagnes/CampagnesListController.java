package com.reboisgabon.client.controllers.campagnes;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.CampagnesApi;
import com.reboisgabon.client.api.endpoints.ExportsApi;
import com.reboisgabon.client.dto.campagnes.Campagne;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.essences.Essence;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.ui.Cellules;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.ExportUtil;
import com.reboisgabon.client.ui.Icones;
import com.reboisgabon.client.ui.Illustrations;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.ErreurApiUtil;
import com.reboisgabon.client.util.EssenceCache;
import com.reboisgabon.client.util.FiltreAutoUtil;
import com.reboisgabon.client.util.SiteCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CampagnesListController implements Initializable {

    @FXML private VBox zoneEntete;
    @FXML private ComboBox<Site> comboSite;
    @FXML private ComboBox<Essence> comboEssence;
    @FXML private DatePicker champDateDebut;
    @FXML private DatePicker champDateFin;
    @FXML private ComboBox<Integer> comboSurvie;
    @FXML private TableView<Campagne> tableCampagnes;
    @FXML private TableColumn<Campagne, Campagne> colonneEssence;
    @FXML private TableColumn<Campagne, Campagne> colonneSite;
    @FXML private TableColumn<Campagne, Campagne> colonneDatePlantation;
    @FXML private TableColumn<Campagne, Campagne> colonneNombrePlants;
    @FXML private TableColumn<Campagne, Campagne> colonneTauxSurvie;
    @FXML private TableColumn<Campagne, Void> colonneActions;
    @FXML private Button boutonPrecedent;
    @FXML private Button boutonSuivant;
    @FXML private Label libelleInfoPagination;

    private final CampagnesApi campagnesApi = new CampagnesApi();
    private final ExportsApi exportsApi = new ExportsApi();
    private PageDrf<Campagne> pageCourante;
    private int numeroPage = 1;
    private String siteDemande;
    private boolean suspendreRecherche;

    private interface FournisseurPage {
        PageDrf<Campagne> charger() throws Exception;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Button export = Composants.boutonExportExcel("Exporter Excel", () -> {});
        export.setOnAction(e -> ExportUtil.excel(export, "campagnes-plantation", exportsApi::campagnesExcel));
        Button nouveau = Composants.bouton("Nouvelle campagne", Icones.AJOUTER, "bouton-primaire");
        nouveau.setOnAction(e -> ouvrirCreation());
        boolean peutCreer = SessionManager.getInstance().peutAcceder("campagnes", "create");
        nouveau.setVisible(peutCreer);
        nouveau.setManaged(peutCreer);
        zoneEntete.getChildren().setAll(Composants.entetePage(Icones.CAMPAGNE, "Campagnes de plantation",
                "Chaque mise en terre : site, essence, date et nombre de plants, avec la survie mesurée depuis.", export, nouveau));

        comboSite.setConverter(convertisseur(s -> s == null ? "Tous les sites" : s.getNom()));
        comboEssence.setConverter(convertisseur(e -> e == null ? "Toutes les essences" : e.getNom()));
        comboSurvie.getItems().addAll(null, 50, 70, 80, 90);
        comboSurvie.setConverter(convertisseur(v -> v == null ? "Toute survie" : "Survie ≥ " + v + " %"));

        SiteCache.getInstance().assurerCharge(sites -> {
            suspendreRecherche = true;
            comboSite.getItems().setAll((Site) null);
            comboSite.getItems().addAll(sites);
            if (siteDemande != null) {
                sites.stream().filter(s -> s.getId().equals(siteDemande)).findFirst().ifPresent(comboSite::setValue);
            }
            suspendreRecherche = false;
        });
        EssenceCache.getInstance().assurerCharge(essences -> {
            comboEssence.getItems().setAll((Essence) null);
            comboEssence.getItems().addAll(essences);
        });

        Cellules.<Campagne>noeud(colonneEssence, c -> {
            StackPane vignette = new StackPane(Illustrations.feuille(Illustrations.especePour(c.getEssenceNom()), 30, Color.web("#1F5136"), Color.web("#E4EFDE")));
            vignette.setMinSize(34, 34);
            vignette.setMaxSize(34, 34);
            vignette.setStyle("-fx-background-color: #F4F7F1; -fx-background-radius: 6;");
            Label nom = new Label(c.getEssenceNom() != null ? c.getEssenceNom() : EssenceCache.getInstance().nomDe(c.getEssence()));
            nom.getStyleClass().add("cellule-principale");
            HBox bloc = new HBox(10, vignette, nom);
            bloc.setAlignment(Pos.CENTER_LEFT);
            return bloc;
        });
        Cellules.<Campagne>texte(colonneSite, c -> c.getSiteNom() != null ? c.getSiteNom() : SiteCache.getInstance().nomDe(c.getSite()));
        Cellules.<Campagne>texte(colonneDatePlantation, c -> Composants.date(c.getDatePlantation()));
        Cellules.<Campagne>droite(colonneNombrePlants, c -> Composants.nombre(c.getNombrePlants()));
        Cellules.<Campagne>noeud(colonneTauxSurvie, c -> Composants.barreSurvie(c.getTauxSurvieMoyen(), 70));
        construireColonneActions();
        Cellules.preparer(tableCampagnes, "Aucune campagne trouvée", "Élargissez la période ou retirez un filtre.");

        FiltreAutoUtil.surValeur(comboSite, this::rechercherSiActif);
        FiltreAutoUtil.surValeur(comboEssence, this::rechercherSiActif);
        FiltreAutoUtil.surValeur(champDateDebut, this::rechercherSiActif);
        FiltreAutoUtil.surValeur(champDateFin, this::rechercherSiActif);
        FiltreAutoUtil.surValeur(comboSurvie, this::rechercherSiActif);

        Platform.runLater(this::rechercher);
    }

    public void filtrerParSite(String idSite) {
        siteDemande = idSite;
        comboSite.getItems().stream().filter(s -> s != null && s.getId().equals(idSite)).findFirst().ifPresent(comboSite::setValue);
    }

    private static <T> StringConverter<T> convertisseur(java.util.function.Function<T, String> libelle) {
        return new StringConverter<>() {
            @Override
            public String toString(T objet) {
                return libelle.apply(objet);
            }

            @Override
            public T fromString(String texte) {
                return null;
            }
        };
    }

    private void construireColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {
            private final Button boutonModifier = Composants.boutonIcone(Icones.MODIFIER, "Modifier", null);
            private final Button boutonSupprimer = Composants.boutonIcone(Icones.SUPPRIMER, "Supprimer", "bouton-icone-danger");
            private final HBox conteneur = new HBox(4, boutonModifier, boutonSupprimer);

            {
                conteneur.setAlignment(Pos.CENTER_LEFT);
            }

            {
                boutonModifier.setOnAction(e -> ouvrirModification(getTableView().getItems().get(getIndex())));
                boutonSupprimer.setOnAction(e -> supprimer(getTableView().getItems().get(getIndex())));
                boolean peutModifier = SessionManager.getInstance().peutAcceder("campagnes", "edit");
                boolean peutSupprimer = SessionManager.getInstance().peutAcceder("campagnes", "delete");
                boutonModifier.setVisible(peutModifier);
                boutonModifier.setManaged(peutModifier);
                boutonSupprimer.setVisible(peutSupprimer);
                boutonSupprimer.setManaged(peutSupprimer);
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                setGraphic(vide || getIndex() < 0 || getIndex() >= getTableView().getItems().size() ? null : conteneur);
            }
        });
    }

    private void rechercherSiActif() {
        if (!suspendreRecherche) {
            rechercher();
        }
    }

    @FXML
    private void reinitialiserFiltres() {
        suspendreRecherche = true;
        siteDemande = null;
        comboSite.setValue(null);
        comboEssence.setValue(null);
        champDateDebut.setValue(null);
        champDateFin.setValue(null);
        comboSurvie.setValue(null);
        suspendreRecherche = false;
        rechercher();
    }

    @FXML
    private void rechercher() {
        Map<String, String> filtres = new HashMap<>();
        if (comboSite.getValue() != null) {
            filtres.put("site", comboSite.getValue().getId());
        } else if (siteDemande != null) {
            filtres.put("site", siteDemande);
        }
        if (comboEssence.getValue() != null) {
            filtres.put("essence", comboEssence.getValue().getId());
        }
        if (champDateDebut.getValue() != null) {
            filtres.put("date_debut", champDateDebut.getValue().toString());
        }
        if (champDateFin.getValue() != null) {
            filtres.put("date_fin", champDateFin.getValue().toString());
        }
        if (comboSurvie.getValue() != null) {
            filtres.put("taux_survie_min", String.valueOf(comboSurvie.getValue()));
        }
        numeroPage = 1;
        chargerPage(() -> campagnesApi.rechercher(filtres));
    }

    @FXML
    private void pagePrecedente() {
        if (pageCourante != null && pageCourante.getPrevious() != null) {
            numeroPage--;
            chargerPage(() -> campagnesApi.rechercherUrl(pageCourante.getPrevious()));
        }
    }

    @FXML
    private void pageSuivante() {
        if (pageCourante != null && pageCourante.getNext() != null) {
            numeroPage++;
            chargerPage(() -> campagnesApi.rechercherUrl(pageCourante.getNext()));
        }
    }

    private void chargerPage(FournisseurPage fournisseur) {
        new Thread(() -> {
            try {
                PageDrf<Campagne> resultat = fournisseur.charger();
                Platform.runLater(() -> {
                    pageCourante = resultat;
                    tableCampagnes.getItems().setAll(resultat.getResults());
                    libelleInfoPagination.setText(resultat.getCount() + " campagne" + (resultat.getCount() > 1 ? "s" : "") + "  ·  page " + numeroPage);
                    boutonPrecedent.setDisable(resultat.getPrevious() == null);
                    boutonSuivant.setDisable(resultat.getNext() == null);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Chargement impossible", "La liste des campagnes n'a pas pu être chargée."));
            }
        }).start();
    }

    private void ouvrirCreation() {
        DialogUtil.<CampagneFormController>ouvrirModal("/com/reboisgabon/client/fxml/campagne-form.fxml", "Nouvelle campagne",
                controleur -> controleur.definirOnSucces(this::rechercher));
    }

    private void ouvrirModification(Campagne campagne) {
        DialogUtil.<CampagneFormController>ouvrirModal("/com/reboisgabon/client/fxml/campagne-form.fxml", "Modifier la campagne",
                controleur -> {
                    controleur.definirCampagneExistante(campagne);
                    controleur.definirOnSucces(this::rechercher);
                });
    }

    private void supprimer(Campagne campagne) {
        boolean confirme = AlertUtil.confirmation("Supprimer cette campagne ?",
                "La campagne du " + Composants.date(campagne.getDatePlantation()) + " et ses suivis de croissance seront définitivement supprimés.");
        if (!confirme) {
            return;
        }
        new Thread(() -> {
            try {
                campagnesApi.supprimer(campagne.getId());
                Platform.runLater(this::rechercher);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Suppression refusée", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Serveur injoignable", "Impossible de joindre le serveur."));
            }
        }).start();
    }
}
