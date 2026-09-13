package com.reboisgabon.client.controllers.campagnes;

import com.reboisgabon.client.api.ApiException;
import com.reboisgabon.client.api.endpoints.CampagnesApi;
import com.reboisgabon.client.api.endpoints.UsersApi;
import com.reboisgabon.client.dto.campagnes.Campagne;
import com.reboisgabon.client.dto.campagnes.CampagneRequete;
import com.reboisgabon.client.dto.common.PageDrf;
import com.reboisgabon.client.dto.essences.Essence;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.dto.utilisateurs.UtilisateurSimple;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.util.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CampagneFormController implements Initializable {

    @FXML private Label libelleTitre;
    @FXML private ComboBox<Site> comboSite;
    @FXML private ComboBox<Essence> comboEssence;
    @FXML private DatePicker champDatePlantation;
    @FXML private TextField champNombrePlants;
    @FXML private ComboBox<UtilisateurSimple> comboResponsable;
    @FXML private Label libelleErreur;
    @FXML private Button boutonEnregistrer;

    private final CampagnesApi campagnesApi = new CampagnesApi();
    private final UsersApi usersApi = new UsersApi();
    private Campagne campagneExistante;
    private Runnable onSucces;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SiteCache.getInstance().assurerCharge(sites -> {
            comboSite.getItems().setAll(sites);
            preselectionnerSiEnAttente();
        });
        EssenceCache.getInstance().assurerCharge(essences -> {
            comboEssence.getItems().setAll(essences);
            preselectionnerSiEnAttente();
        });
        chargerResponsables();
    }

    public void definirCampagneExistante(Campagne campagne) {
        this.campagneExistante = campagne;
        libelleTitre.setText("Modifier la campagne");
        champDatePlantation.setValue(campagne.getDatePlantation());
        champNombrePlants.setText(campagne.getNombrePlants() != null ? campagne.getNombrePlants().toString() : "");
        preselectionnerSiEnAttente();
    }

    public void definirOnSucces(Runnable onSucces) {
        this.onSucces = onSucces;
    }

    private void preselectionnerSiEnAttente() {
        if (campagneExistante == null) {
            return;
        }
        comboSite.getItems().stream()
                .filter(site -> site.getId().equals(campagneExistante.getSite()))
                .findFirst()
                .ifPresent(comboSite::setValue);
        comboEssence.getItems().stream()
                .filter(essence -> essence.getId().equals(campagneExistante.getEssence()))
                .findFirst()
                .ifPresent(comboEssence::setValue);
        comboResponsable.getItems().stream()
                .filter(utilisateur -> utilisateur.getId().equals(campagneExistante.getResponsable()))
                .findFirst()
                .ifPresent(comboResponsable::setValue);
    }

    private void chargerResponsables() {
        new Thread(() -> {
            try {
                var page = usersApi.lister();
                Platform.runLater(() -> {
                    comboResponsable.getItems().setAll(page.getResults());
                    preselectionnerSiEnAttente();
                });
            } catch (Exception e) {
                Platform.runLater(() -> afficherErreur("Impossible de charger la liste des responsables."));
            }
        }).start();
    }

    @FXML
    private void enregistrer() {
        if (comboSite.getValue() == null || comboEssence.getValue() == null || champDatePlantation.getValue() == null) {
            afficherErreur("Site, essence et date de plantation sont obligatoires.");
            return;
        }
        CampagneRequete requete = new CampagneRequete();
        requete.setSite(comboSite.getValue().getId());
        requete.setEssence(comboEssence.getValue().getId());
        requete.setDatePlantation(champDatePlantation.getValue());
        requete.setResponsable(comboResponsable.getValue() != null ? comboResponsable.getValue().getId() : null);
        try {
            requete.setNombrePlants(Integer.parseInt(champNombrePlants.getText()));
        } catch (NumberFormatException e) {
            afficherErreur("Le nombre de plants doit être un entier valide.");
            return;
        }
        masquerErreur();
        boutonEnregistrer.setDisable(true);
        new Thread(() -> {
            try {
                if (campagneExistante == null) {
                    campagnesApi.creer(requete);
                } else {
                    campagnesApi.modifier(campagneExistante.getId(), requete);
                }
                Platform.runLater(() -> {
                    if (onSucces != null) {
                        onSucces.run();
                    }
                    fermer();
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    boutonEnregistrer.setDisable(false);
                    afficherErreur(ErreurApiUtil.message(e));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    boutonEnregistrer.setDisable(false);
                    afficherErreur("Impossible de joindre le serveur.");
                });
            }
        }).start();
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        ((Stage) champNombrePlants.getScene().getWindow()).close();
    }

    private void afficherErreur(String message) {
        libelleErreur.setText(message);
        libelleErreur.setVisible(true);
        libelleErreur.setManaged(true);
    }

    private void masquerErreur() {
        libelleErreur.setVisible(false);
        libelleErreur.setManaged(false);
    }

    public static class CampagnesListController implements Initializable {

        @FXML private ComboBox<Site> comboSite;
        @FXML private ComboBox<Essence> comboEssence;
        @FXML private DatePicker champDateDebut;
        @FXML private DatePicker champDateFin;
        @FXML private Button boutonNouvelleCampagne;
        @FXML private TableView<Campagne> tableCampagnes;
        @FXML private TableColumn<Campagne, String> colonneSite;
        @FXML private TableColumn<Campagne, String> colonneEssence;
        @FXML private TableColumn<Campagne, String> colonneDatePlantation;
        @FXML private TableColumn<Campagne, Integer> colonneNombrePlants;
        @FXML private TableColumn<Campagne, String> colonneTauxSurvie;
        @FXML private TableColumn<Campagne, Void> colonneActions;
        @FXML private Button boutonPrecedent;
        @FXML private Button boutonSuivant;
        @FXML private Label libelleInfoPagination;

        private final CampagnesApi campagnesApi = new CampagnesApi();
        private PageDrf<Campagne> pageCourante;

        private interface FournisseurPage {
            PageDrf<Campagne> charger() throws Exception;
        }

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            SiteCache.getInstance().assurerCharge(sites -> {
                comboSite.getItems().add(null);
                comboSite.getItems().addAll(sites);
            });
            EssenceCache.getInstance().assurerCharge(essences -> {
                comboEssence.getItems().add(null);
                comboEssence.getItems().addAll(essences);
            });
            colonneSite.setCellValueFactory(data -> new ReadOnlyStringWrapper(SiteCache.getInstance().nomDe(data.getValue().getSite())));
            colonneEssence.setCellValueFactory(data -> new ReadOnlyStringWrapper(EssenceCache.getInstance().nomDe(data.getValue().getEssence())));
            colonneDatePlantation.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                    data.getValue().getDatePlantation() != null ? data.getValue().getDatePlantation().toString() : ""));
            colonneNombrePlants.setCellValueFactory(new PropertyValueFactory<>("nombrePlants"));
            colonneTauxSurvie.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                    data.getValue().getTauxSurvieMoyen() != null ? data.getValue().getTauxSurvieMoyen().toString() : "—"));
            construireColonneActions();
            boolean peutCreer = SessionManager.getInstance().peutAcceder("campagnes", "create");
            boutonNouvelleCampagne.setVisible(peutCreer);
            boutonNouvelleCampagne.setManaged(peutCreer);
            rechercher();
        }

        private void construireColonneActions() {
            colonneActions.setCellFactory(colonne -> new TableCell<>() {

                private final Button boutonModifier = new Button("Modifier");
                private final Button boutonSupprimer = new Button("Supprimer");
                private final HBox conteneur = new HBox(8, boutonModifier, boutonSupprimer);

                {
                    boutonModifier.getStyleClass().add("bouton-secondaire");
                    boutonSupprimer.getStyleClass().add("bouton-secondaire");
                    boutonModifier.setOnAction(evenement -> ouvrirModification(getTableView().getItems().get(getIndex())));
                    boutonSupprimer.setOnAction(evenement -> supprimer(getTableView().getItems().get(getIndex())));
                }

                @Override
                protected void updateItem(Void item, boolean vide) {
                    super.updateItem(item, vide);
                    if (vide) {
                        setGraphic(null);
                        return;
                    }
                    boolean peutModifier = SessionManager.getInstance().peutAcceder("campagnes", "edit");
                    boolean peutSupprimer = SessionManager.getInstance().peutAcceder("campagnes", "delete");
                    boutonModifier.setVisible(peutModifier);
                    boutonModifier.setManaged(peutModifier);
                    boutonSupprimer.setVisible(peutSupprimer);
                    boutonSupprimer.setManaged(peutSupprimer);
                    setGraphic(conteneur);
                }
            });
        }

        @FXML
        private void rechercher() {
            Map<String, String> filtres = new HashMap<>();
            if (comboSite.getValue() != null) {
                filtres.put("site", comboSite.getValue().getId());
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
            chargerPage(() -> campagnesApi.rechercher(filtres));
        }

        @FXML
        private void pagePrecedente() {
            if (pageCourante != null && pageCourante.getPrevious() != null) {
                chargerPage(() -> campagnesApi.rechercherUrl(pageCourante.getPrevious()));
            }
        }

        @FXML
        private void pageSuivante() {
            if (pageCourante != null && pageCourante.getNext() != null) {
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
                        libelleInfoPagination.setText(resultat.getCount() + " résultats");
                        boutonPrecedent.setDisable(resultat.getPrevious() == null);
                        boutonSuivant.setDisable(resultat.getNext() == null);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de charger les campagnes."));
                }
            }).start();
        }

        @FXML
        private void ouvrirCreation() {
            DialogUtil.<CampagneFormController>ouvrirModal(
                    "/com/reboisgabon/client/fxml/campagne-form.fxml",
                    "Nouvelle campagne",
                    controleur -> controleur.definirOnSucces(this::rechercher));
        }

        private void ouvrirModification(Campagne campagne) {
            DialogUtil.<CampagneFormController>ouvrirModal(
                    "/com/reboisgabon/client/fxml/campagne-form.fxml",
                    "Modifier la campagne",
                    controleur -> {
                        controleur.definirCampagneExistante(campagne);
                        controleur.definirOnSucces(this::rechercher);
                    });
        }

        private void supprimer(Campagne campagne) {
            boolean confirme = AlertUtil.confirmation("Confirmation", "Supprimer cette campagne ?");
            if (!confirme) {
                return;
            }
            new Thread(() -> {
                try {
                    campagnesApi.supprimer(campagne.getId());
                    Platform.runLater(this::rechercher);
                } catch (ApiException e) {
                    Platform.runLater(() -> AlertUtil.erreur("Erreur", ErreurApiUtil.message(e)));
                } catch (Exception e) {
                    Platform.runLater(() -> AlertUtil.erreur("Erreur", "Impossible de joindre le serveur."));
                }
            }).start();
        }
    }
}