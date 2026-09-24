package com.reboisgabon.client.controllers.sites;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.ExportsApi;
import com.reboisgabon.client.api.endpoints.SitesApi;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.dto.sites.StatutSite;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.ui.Cellules;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.ExportUtil;
import com.reboisgabon.client.ui.Icones;
import com.reboisgabon.client.ui.Navigation;
import com.reboisgabon.client.util.AlertUtil;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.ErreurApiUtil;
import com.reboisgabon.client.util.FiltreAutoUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SitesListController implements Initializable {

    public static final List<String> PROVINCES = List.of("Estuaire", "Haut-Ogooué", "Moyen-Ogooué", "Ngounié", "Nyanga",
            "Ogooué-Ivindo", "Ogooué-Lolo", "Ogooué-Maritime", "Woleu-Ntem");

    @FXML private VBox zoneEntete;
    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> comboProvince;
    @FXML private ComboBox<StatutSite> comboStatut;
    @FXML private ComboBox<Integer> comboSurvie;
    @FXML private TableView<Site> tableSites;
    @FXML private TableColumn<Site, Site> colonneNom;
    @FXML private TableColumn<Site, Site> colonneProvince;
    @FXML private TableColumn<Site, Site> colonneSuperficie;
    @FXML private TableColumn<Site, Site> colonneCampagnes;
    @FXML private TableColumn<Site, Site> colonneStatut;
    @FXML private TableColumn<Site, Site> colonneTauxSurvie;
    @FXML private TableColumn<Site, Void> colonneActions;
    @FXML private Button boutonPrecedent;
    @FXML private Button boutonSuivant;
    @FXML private Label libelleInfoPagination;

    private final SitesApi sitesApi = new SitesApi();
    private final ExportsApi exportsApi = new ExportsApi();
    private PageDrf<Site> pageCourante;
    private int numeroPage = 1;

    private interface FournisseurPage {
        PageDrf<Site> charger() throws Exception;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Button export = Composants.boutonExportExcel("Exporter Excel", () -> {});
        export.setOnAction(e -> ExportUtil.excel(export, "sites-reboisement", exportsApi::sitesExcel));
        Button nouveau = Composants.bouton("Nouveau site", Icones.AJOUTER, "bouton-primaire");
        nouveau.setOnAction(e -> ouvrirCreation());
        boolean peutCreer = SessionManager.getInstance().peutAcceder("sites", "create");
        nouveau.setVisible(peutCreer);
        nouveau.setManaged(peutCreer);
        zoneEntete.getChildren().setAll(Composants.entetePage(Icones.SITE, "Registre des sites",
                "Les parcelles de reboisement, leur statut et le taux de survie calculé à partir des contrôles.", export, nouveau));

        comboProvince.getItems().add(null);
        comboProvince.getItems().addAll(PROVINCES);
        comboProvince.setConverter(convertisseur(p -> p == null ? "Toutes les provinces" : p));
        comboStatut.getItems().add(null);
        comboStatut.getItems().addAll(StatutSite.values());
        comboStatut.setConverter(convertisseur(s -> s == null ? "Tous les statuts" : Composants.libelleStatutSite(s.name())));
        comboSurvie.getItems().addAll(null, 50, 70, 80, 90);
        comboSurvie.setConverter(convertisseur(v -> v == null ? "Toute survie" : "Survie ≥ " + v + " %"));

        Cellules.<Site>double_(colonneNom, Site::getNom, Site::getLocalite);
        Cellules.<Site>texte(colonneProvince, Site::getProvince);
        Cellules.<Site>droite(colonneSuperficie, s -> s.getSuperficieHectares() == null ? null : Composants.decimal(s.getSuperficieHectares()) + " ha");
        Cellules.<Site>droite(colonneCampagnes, s -> s.getNombreCampagnes() == null ? "0" : String.valueOf(s.getNombreCampagnes()));
        Cellules.<Site>noeud(colonneStatut, s -> Composants.pastilleStatutSite(s.getStatut() == null ? null : s.getStatut().name()));
        Cellules.<Site>noeud(colonneTauxSurvie, s -> Composants.barreSurvie(s.getTauxSurvieMoyen(), 70));
        construireColonneActions();
        Cellules.preparer(tableSites, "Aucun site ne correspond", "Modifiez les filtres ou créez un nouveau site de reboisement.");
        tableSites.setRowFactory(t -> {
            var ligne = new javafx.scene.control.TableRow<Site>();
            ligne.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !ligne.isEmpty()) {
                    voirSurCarte(ligne.getItem());
                }
            });
            return ligne;
        });

        FiltreAutoUtil.surSaisie(champRecherche, this::rechercher);
        FiltreAutoUtil.surValeur(comboProvince, this::rechercher);
        FiltreAutoUtil.surValeur(comboStatut, this::rechercher);
        FiltreAutoUtil.surValeur(comboSurvie, this::rechercher);

        rechercher();
    }

    static <T> StringConverter<T> convertisseur(java.util.function.Function<T, String> libelle) {
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
            private final Button boutonCarte = Composants.boutonIcone(Icones.CARTE, "Voir la fiche sur la carte", null);
            private final Button boutonModifier = Composants.boutonIcone(Icones.MODIFIER, "Modifier", null);
            private final Button boutonSupprimer = Composants.boutonIcone(Icones.SUPPRIMER, "Supprimer", "bouton-icone-danger");
            private final HBox conteneur = new HBox(4, boutonCarte, boutonModifier, boutonSupprimer);

            {
                conteneur.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }

            {
                boutonCarte.setOnAction(e -> voirSurCarte(getTableView().getItems().get(getIndex())));
                boutonModifier.setOnAction(e -> ouvrirModification(getTableView().getItems().get(getIndex())));
                boutonSupprimer.setOnAction(e -> supprimer(getTableView().getItems().get(getIndex())));
                boolean peutModifier = SessionManager.getInstance().peutAcceder("sites", "edit");
                boolean peutSupprimer = SessionManager.getInstance().peutAcceder("sites", "delete");
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

    private void voirSurCarte(Site site) {
        Navigation.<CarteController>aller(Navigation.Ecran.CARTE, c -> c.focaliserSite(site.getId()));
    }

    @FXML
    private void reinitialiserFiltres() {
        champRecherche.clear();
        comboProvince.setValue(null);
        comboStatut.setValue(null);
        comboSurvie.setValue(null);
        rechercher();
    }

    @FXML
    private void rechercher() {
        Map<String, String> filtres = new HashMap<>();
        filtres.put("search", champRecherche.getText());
        filtres.put("province", comboProvince.getValue());
        if (comboStatut.getValue() != null) {
            filtres.put("statut", comboStatut.getValue().name());
        }
        if (comboSurvie.getValue() != null) {
            filtres.put("taux_survie_min", String.valueOf(comboSurvie.getValue()));
        }
        numeroPage = 1;
        chargerPage(() -> sitesApi.rechercher(filtres));
    }

    @FXML
    private void pagePrecedente() {
        if (pageCourante != null && pageCourante.getPrevious() != null) {
            numeroPage--;
            chargerPage(() -> sitesApi.rechercherUrl(pageCourante.getPrevious()));
        }
    }

    @FXML
    private void pageSuivante() {
        if (pageCourante != null && pageCourante.getNext() != null) {
            numeroPage++;
            chargerPage(() -> sitesApi.rechercherUrl(pageCourante.getNext()));
        }
    }

    private void chargerPage(FournisseurPage fournisseur) {
        new Thread(() -> {
            try {
                PageDrf<Site> resultat = fournisseur.charger();
                Platform.runLater(() -> {
                    pageCourante = resultat;
                    tableSites.getItems().setAll(resultat.getResults());
                    libelleInfoPagination.setText(resultat.getCount() + " site" + (resultat.getCount() > 1 ? "s" : "") + "  ·  page " + numeroPage);
                    boutonPrecedent.setDisable(resultat.getPrevious() == null);
                    boutonSuivant.setDisable(resultat.getNext() == null);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Chargement impossible", "La liste des sites n'a pas pu être chargée."));
            }
        }).start();
    }

    private void ouvrirCreation() {
        DialogUtil.<SiteFormController>ouvrirModal("/com/reboisgabon/client/fxml/site-form.fxml", "Nouveau site",
                controleur -> controleur.definirOnSucces(this::rechercher));
    }

    private void ouvrirModification(Site site) {
        DialogUtil.<SiteFormController>ouvrirModal("/com/reboisgabon/client/fxml/site-form.fxml", "Modifier le site",
                controleur -> {
                    controleur.definirSiteExistant(site);
                    controleur.definirOnSucces(this::rechercher);
                });
    }

    private void supprimer(Site site) {
        boolean confirme = AlertUtil.confirmation("Supprimer ce site ?",
                "« " + site.getNom() + " » et toutes ses campagnes et suivis seront définitivement supprimés.");
        if (!confirme) {
            return;
        }
        new Thread(() -> {
            try {
                sitesApi.supprimer(site.getId());
                Platform.runLater(this::rechercher);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertUtil.erreur("Suppression refusée", ErreurApiUtil.message(e)));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.erreur("Serveur injoignable", "Impossible de joindre le serveur."));
            }
        }).start();
    }
}
