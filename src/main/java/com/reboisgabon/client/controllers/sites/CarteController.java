package com.reboisgabon.client.controllers.sites;

import com.fasterxml.jackson.databind.JsonNode;
import com.reboisgabon.client.api.endpoints.CampagnesApi;
import com.reboisgabon.client.api.endpoints.DashboardApi;
import com.reboisgabon.client.api.endpoints.GeolocalisationApi;
import com.reboisgabon.client.api.endpoints.SitesApi;
import com.reboisgabon.client.controllers.campagnes.CampagnesListController;
import com.reboisgabon.client.dto.campagnes.Campagne;
import com.reboisgabon.client.dto.sites.Site;
import com.reboisgabon.client.session.SessionManager;
import com.reboisgabon.client.ui.CarteProvinces;
import com.reboisgabon.client.ui.Composants;
import com.reboisgabon.client.ui.Icones;
import com.reboisgabon.client.ui.Illustrations;
import com.reboisgabon.client.ui.Navigation;
import com.reboisgabon.client.ui.PontCarte;
import com.reboisgabon.client.util.DialogUtil;
import com.reboisgabon.client.util.JsonMapper;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.InputStream;
import java.net.URL;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class CarteController implements Initializable {

    @FXML private WebView vueCarte;
    @FXML private Label libelleZone;
    @FXML private VBox legende;
    @FXML private VBox fiche;
    @FXML private ScrollPane defilementFiche;
    @FXML private ToggleButton fondSatellite;
    @FXML private ToggleButton fondPlan;
    @FXML private ToggleButton fondEpure;

    private final SitesApi sitesApi = new SitesApi();
    private final CampagnesApi campagnesApi = new CampagnesApi();
    private final DashboardApi dashboardApi = new DashboardApi();
    private final GeolocalisationApi geolocalisationApi = new GeolocalisationApi();

    private PontCarte pont;
    private boolean carteChargee;
    private String actionEnAttente;
    private JsonNode sitesGeojson;
    private JsonNode statsProvinces;
    private JsonNode apercu;
    private String siteAffiche;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ToggleGroup groupe = new ToggleGroup();
        fondSatellite.setToggleGroup(groupe);
        fondPlan.setToggleGroup(groupe);
        fondEpure.setToggleGroup(groupe);
        groupe.selectedToggleProperty().addListener((o, ancien, nouveau) -> {
            if (nouveau == null) {
                groupe.selectToggle(ancien);
            }
        });
        vueCarte.setContextMenuEnabled(false);
        vueCarte.widthProperty().addListener((o, a, n) -> executer("window.recalculerTaille && window.recalculerTaille()"));
        vueCarte.heightProperty().addListener((o, a, n) -> executer("window.recalculerTaille && window.recalculerTaille()"));
        construireLegende();
        fiche.getChildren().setAll(Composants.chargement("Chargement de la carte…"));
        pont = new PontCarte(this::ouvrirSite, this::ouvrirProvince);
        chargerDonnees();
    }

    public void focaliserSite(String id) {
        if (carteChargee) {
            ouvrirSite(id);
        } else {
            actionEnAttente = "site:" + id;
        }
    }

    public void focaliserProvince(String nom) {
        if (carteChargee) {
            ouvrirProvince(nom);
        } else {
            actionEnAttente = "province:" + nom;
        }
    }

    private void chargerDonnees() {
        Thread tache = new Thread(() -> {
            try {
                String geojson = geolocalisationApi.sitesGeojson();
                JsonNode provinces = dashboardApi.provinces();
                JsonNode vueGenerale = dashboardApi.overview();
                String html = construireHtml();
                Platform.runLater(() -> {
                    try {
                        sitesGeojson = JsonMapper.instance().readTree(geojson);
                    } catch (Exception e) {
                        sitesGeojson = JsonMapper.instance().createObjectNode();
                    }
                    statsProvinces = provinces;
                    apercu = vueGenerale;
                    afficherVueNationale();
                    vueCarte.getEngine().getLoadWorker().stateProperty().addListener((obs, ancien, etat) -> {
                        if (etat == Worker.State.SUCCEEDED) {
                            initialiserCarte(geojson);
                        }
                    });
                    vueCarte.getEngine().loadContent(html, "text/html");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label message = new Label("La carte n'a pas pu être chargée. Vérifiez que l'API est démarrée puis rouvrez cette page.");
                    message.getStyleClass().add("message-erreur");
                    message.setWrapText(true);
                    fiche.getChildren().setAll(message);
                });
            }
        });
        tache.setDaemon(true);
        tache.start();
    }

    private String construireHtml() throws Exception {
        String gabarit = lireRessource("/com/reboisgabon/client/carte/carte.html");
        return gabarit
                .replace("/*LEAFLET_CSS*/", lireRessource("/com/reboisgabon/client/carte/leaflet.css"))
                .replace("/*LEAFLET_JS*/", lireRessource("/com/reboisgabon/client/carte/leaflet.js"));
    }

    private String lireRessource(String chemin) throws Exception {
        try (InputStream flux = getClass().getResourceAsStream(chemin)) {
            return new String(flux.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void initialiserCarte(String geojson) {
        JSObject fenetre = (JSObject) vueCarte.getEngine().executeScript("window");
        fenetre.setMember("pontJava", pont);
        List<Map<String, Object>> stats = new ArrayList<>();
        for (JsonNode p : statsProvinces) {
            Map<String, Object> s = new HashMap<>();
            s.put("province", p.path("province").asText());
            s.put("survie", p.hasNonNull("taux_survie_moyen") ? p.get("taux_survie_moyen").asDouble() : null);
            stats.add(s);
        }
        try {
            String provinces = JsonMapper.instance().writeValueAsString(CarteProvinces.provinces());
            String statsJson = JsonMapper.instance().writeValueAsString(stats);
            executer("window.initialiser(" + provinces + ", " + geojson + ", " + statsJson + ")");
        } catch (Exception e) {
            System.err.println("[carte] " + e.getMessage());
        }
        carteChargee = true;
        if (actionEnAttente != null) {
            String action = actionEnAttente;
            actionEnAttente = null;
            if (action.startsWith("site:")) {
                ouvrirSite(action.substring(5));
            } else if (action.startsWith("province:")) {
                ouvrirProvince(action.substring(9));
            }
        }
    }

    private Object executer(String script) {
        try {
            return vueCarte.getEngine().executeScript(script);
        } catch (Exception e) {
            return null;
        }
    }

    private String js(String texte) {
        return "'" + texte.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private void construireLegende() {
        Label titre = new Label("STATUT DU SITE");
        titre.getStyleClass().add("cle-etiquette");
        legende.getChildren().add(titre);
        String[][] statuts = {{"EN_COURS", "#3E8E5A"}, {"TERMINE", "#1F5136"}, {"PLANIFIE", "#2E6A9E"}, {"SUSPENDU", "#9A5B34"}};
        for (String[] s : statuts) {
            Circle puce = new Circle(5.5, Color.web(s[1]));
            puce.setStroke(Color.WHITE);
            puce.setStrokeWidth(1.5);
            Label l = new Label(Composants.libelleStatutSite(s[0]));
            l.getStyleClass().add("texte-petit");
            HBox item = new HBox(7, puce, l);
            item.setAlignment(Pos.CENTER_LEFT);
            legende.getChildren().add(item);
        }
        Label taille = new Label("Taille du point : superficie");
        taille.getStyleClass().add("texte-petit");
        Label titre2 = new Label("SURVIE PAR PROVINCE");
        titre2.getStyleClass().add("cle-etiquette");
        HBox echelle = new HBox(0);
        for (double t : new double[]{50, 65, 75, 85, 95}) {
            Rectangle r = new Rectangle(26, 8, Composants.couleurSurvie(t));
            echelle.getChildren().add(r);
        }
        Label bornes = new Label("< 60 %                  ≥ 90 %");
        bornes.getStyleClass().add("texte-petit");
        legende.getChildren().addAll(taille, new Region(), titre2, echelle, bornes);
    }

    @FXML
    private void vueNationale() {
        executer("window.vueNationale()");
        afficherVueNationale();
    }

    @FXML
    private void fondSatellite() {
        executer("window.changerFond('satellite')");
    }

    @FXML
    private void fondPlan() {
        executer("window.changerFond('plan')");
    }

    @FXML
    private void fondEpure() {
        executer("window.changerFond('epure')");
    }

    private double nombre(JsonNode n, String champ) {
        return n != null && n.hasNonNull(champ) ? n.get(champ).asDouble() : 0;
    }

    private Double nombreOuNull(JsonNode n, String champ) {
        return n != null && n.hasNonNull(champ) ? n.get(champ).asDouble() : null;
    }

    private void afficherVueNationale() {
        siteAffiche = null;
        libelleZone.setText("Gabon — vue nationale");
        Label titre = new Label("Territoire national");
        titre.getStyleClass().add("titre-page");
        Label sousTitre = new Label("Cliquez un site pour ouvrir sa fiche, ou une province pour son bilan.");
        sousTitre.getStyleClass().add("texte-muet");
        sousTitre.setWrapText(true);

        GridPane chiffres = new GridPane();
        chiffres.setHgap(14);
        chiffres.setVgap(12);
        chiffres.add(chiffre(String.valueOf((int) nombre(apercu, "total_sites")), "sites"), 0, 0);
        chiffres.add(chiffre(Composants.decimal(nombre(apercu, "superficie_totale_hectares")) + " ha", "reboisés"), 1, 0);
        chiffres.add(chiffre(Composants.nombre(nombre(apercu, "total_plants_plantes")), "plants"), 0, 1);
        chiffres.add(chiffre(Composants.pourcentage(nombreOuNull(apercu, "taux_survie_global")), "survie moyenne"), 1, 1);

        Label titreProvinces = new Label("LES 9 PROVINCES");
        titreProvinces.getStyleClass().add("cle-etiquette");
        VBox provinces = new VBox();
        List<JsonNode> liste = new ArrayList<>();
        statsProvinces.forEach(liste::add);
        liste.sort((a, b) -> a.path("province").asText().compareTo(b.path("province").asText()));
        Set<String> presentes = new LinkedHashSet<>();
        for (JsonNode p : liste) {
            presentes.add(CarteProvinces.normaliser(p.path("province").asText()));
            provinces.getChildren().add(ligneProvince(p.path("province").asText(), (int) nombre(p, "nb_sites"), nombreOuNull(p, "taux_survie_moyen")));
        }
        for (JsonNode f : CarteProvinces.provinces().get("features")) {
            String nom = f.path("properties").path("nom").asText();
            if (!presentes.contains(CarteProvinces.normaliser(nom))) {
                provinces.getChildren().add(ligneProvince(nom, 0, null));
            }
        }
        fiche.getChildren().setAll(new VBox(4, titre, sousTitre), chiffres, new VBox(6, titreProvinces, provinces));
        defilementFiche.setVvalue(0);
    }

    private HBox ligneProvince(String nom, int sites, Double survie) {
        Label libelle = new Label(nom);
        libelle.getStyleClass().add("titre-3");
        Label detail = new Label(sites == 0 ? "aucun site" : sites + " site" + (sites > 1 ? "s" : ""));
        detail.getStyleClass().add("texte-petit");
        VBox textes = new VBox(0, libelle, detail);
        HBox.setHgrow(textes, Priority.ALWAYS);
        HBox ligne = new HBox(8, textes, Composants.barreSurvie(survie, 54), Icones.icone(Icones.CHEVRON, 16, Color.web("#66766A")));
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.getStyleClass().addAll("ligne-liste", "ligne-liste-cliquable");
        ligne.setPadding(new Insets(8, 2, 8, 2));
        ligne.setOnMouseClicked(e -> ouvrirProvince(nom));
        return ligne;
    }

    private VBox chiffre(String valeur, String libelle) {
        Label v = new Label(valeur);
        v.getStyleClass().add("chiffre-moyen");
        Label l = new Label(libelle);
        l.getStyleClass().add("texte-petit");
        return new VBox(0, v, l);
    }

    private void ouvrirProvince(String nom) {
        executer("window.focaliserProvince(" + js(nom) + ")");
        siteAffiche = null;
        libelleZone.setText("Province : " + nom);
        JsonNode stat = null;
        for (JsonNode p : statsProvinces) {
            if (CarteProvinces.normaliser(p.path("province").asText()).equals(CarteProvinces.normaliser(nom))) {
                stat = p;
            }
        }
        Button retour = Composants.bouton("Vue nationale", Icones.RECENTRER, "bouton-lien");
        retour.setOnAction(e -> vueNationale());
        Label titre = new Label(nom);
        titre.getStyleClass().add("titre-page");
        Label sousTitre = new Label("Province du Gabon");
        sousTitre.getStyleClass().add("texte-muet");

        VBox contenu = new VBox(16, retour, new VBox(2, titre, sousTitre));
        if (stat == null) {
            contenu.getChildren().add(Composants.etatVide("Aucun site dans cette province",
                    "Aucun site de reboisement n'y est encore enregistré. Créez-en un depuis le registre des sites."));
            fiche.getChildren().setAll(contenu);
            return;
        }
        GridPane chiffres = new GridPane();
        chiffres.setHgap(14);
        chiffres.setVgap(12);
        chiffres.add(chiffre(String.valueOf((int) nombre(stat, "nb_sites")), "sites"), 0, 0);
        chiffres.add(chiffre(Composants.decimal(nombre(stat, "superficie_totale")) + " ha", "reboisés"), 1, 0);
        chiffres.add(chiffre(Composants.nombre(nombre(stat, "total_plants")), "plants"), 0, 1);
        chiffres.add(chiffre(Composants.pourcentage(nombreOuNull(stat, "taux_survie_moyen")), "survie moyenne"), 1, 1);

        Label titreSites = new Label("SITES DE LA PROVINCE");
        titreSites.getStyleClass().add("cle-etiquette");
        VBox sites = new VBox();
        for (JsonNode f : sitesGeojson.path("features")) {
            JsonNode p = f.path("properties");
            if (CarteProvinces.normaliser(p.path("province").asText()).equals(CarteProvinces.normaliser(nom))) {
                sites.getChildren().add(ligneSite(p));
            }
        }
        contenu.getChildren().addAll(chiffres, new VBox(6, titreSites, sites));
        fiche.getChildren().setAll(contenu);
        defilementFiche.setVvalue(0);
    }

    private HBox ligneSite(JsonNode p) {
        String id = p.path("id").asText();
        Circle puce = new Circle(5.5, couleurStatut(p.path("statut").asText()));
        Label nom = new Label(p.path("nom").asText());
        nom.getStyleClass().add("titre-3");
        nom.setWrapText(true);
        Label detail = new Label(p.path("localite").asText() + "  ·  " + Composants.decimal(nombre(p, "superficie_hectares")) + " ha");
        detail.getStyleClass().add("texte-petit");
        VBox textes = new VBox(1, nom, detail);
        HBox.setHgrow(textes, Priority.ALWAYS);
        textes.setMinWidth(0);
        Label survie = new Label(Composants.pourcentage(nombreOuNull(p, "taux_survie_moyen")));
        survie.getStyleClass().add("texte-corps");
        HBox ligne = new HBox(9, puce, textes, survie);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.getStyleClass().addAll("ligne-liste", "ligne-liste-cliquable");
        ligne.setPadding(new Insets(8, 2, 8, 2));
        ligne.setOnMouseClicked(e -> ouvrirSite(id));
        return ligne;
    }

    private Color couleurStatut(String statut) {
        return switch (statut) {
            case "EN_COURS" -> Color.web("#3E8E5A");
            case "TERMINE" -> Color.web("#1F5136");
            case "SUSPENDU" -> Color.web("#9A5B34");
            default -> Color.web("#2E6A9E");
        };
    }

    private void ouvrirSite(String id) {
        siteAffiche = id;
        executer("window.focaliserSite(" + js(id) + ")");
        fiche.getChildren().setAll(Composants.chargement("Ouverture de la fiche du site…"));
        Thread tache = new Thread(() -> {
            try {
                Site site = sitesApi.obtenir(id);
                JsonNode score = null;
                try {
                    score = sitesApi.scoreEcologique(id);
                } catch (Exception ignore) {
                }
                Map<String, String> filtres = new HashMap<>();
                filtres.put("site", id);
                filtres.put("page_size", "50");
                List<Campagne> campagnes = campagnesApi.rechercher(filtres).getResults();
                JsonNode voisins = null;
                if (site.getLatitude() != null && site.getLongitude() != null) {
                    try {
                        voisins = geolocalisationApi.sitesProximite(site.getLatitude().doubleValue(), site.getLongitude().doubleValue(), 80, 1);
                    } catch (Exception ignore) {
                    }
                }
                JsonNode scoreFinal = score;
                JsonNode voisinsFinal = voisins;
                Platform.runLater(() -> {
                    if (id.equals(siteAffiche)) {
                        afficherFicheSite(site, scoreFinal, campagnes, voisinsFinal);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label message = new Label("La fiche de ce site n'a pas pu être chargée.");
                    message.getStyleClass().add("message-erreur");
                    fiche.getChildren().setAll(message);
                });
            }
        });
        tache.setDaemon(true);
        tache.start();
    }

    private void afficherFicheSite(Site site, JsonNode score, List<Campagne> campagnes, JsonNode voisins) {
        libelleZone.setText(site.getNom());
        String statut = site.getStatut() != null ? site.getStatut().name() : null;

        Button retour = Composants.bouton(site.getProvince() != null && !site.getProvince().isBlank() ? "Bilan de la province " + site.getProvince() : "Vue nationale", Icones.CARTE, "bouton-lien");
        retour.setOnAction(e -> {
            if (site.getProvince() != null && !site.getProvince().isBlank()) {
                ouvrirProvince(site.getProvince());
            } else {
                vueNationale();
            }
        });

        Label nom = new Label(site.getNom());
        nom.getStyleClass().add("titre-page");
        nom.setWrapText(true);
        Label lieu = new Label(site.getLocalite() + (site.getProvince() != null ? "  ·  " + site.getProvince() : ""));
        lieu.getStyleClass().add("sous-titre");
        HBox tete = new HBox(8, Composants.pastilleStatutSite(statut));
        if (score != null && score.hasNonNull("classe")) {
            tete.getChildren().add(Composants.pastille("Score " + Composants.decimal(score.get("score_global").asDouble()), "or"));
        }

        Set<String> essences = new LinkedHashSet<>();
        long totalPlants = 0;
        for (Campagne c : campagnes) {
            if (c.getEssenceNom() != null) {
                essences.add(c.getEssenceNom());
            }
            totalPlants += c.getNombrePlants() != null ? c.getNombrePlants() : 0;
        }

        HBox dessins = new HBox(-4);
        dessins.setAlignment(Pos.BOTTOM_RIGHT);
        dessins.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        dessins.setMouseTransparent(true);
        int n = 0;
        for (String essence : essences) {
            if (n++ >= 3) {
                break;
            }
            dessins.getChildren().add(Illustrations.feuille(Illustrations.especePour(essence), 62, Color.web("#3E8E5A"), Color.web("#FFFFFF")));
        }

        StackPane planche = new StackPane();
        planche.setStyle("-fx-background-color: #F6F8F3; -fx-background-radius: 6; -fx-border-color: #DDE3D6; -fx-border-radius: 6;");
        planche.setPadding(new Insets(12, 14, 8, 14));
        VBox jauge = jaugeSurvie(site.getTauxSurvieMoyen());
        StackPane.setAlignment(jauge, Pos.TOP_LEFT);
        StackPane.setAlignment(dessins, Pos.BOTTOM_RIGHT);
        planche.getChildren().addAll(dessins, jauge);
        planche.setMinHeight(116);

        String coordonnees = site.getLatitude() != null && site.getLongitude() != null
                ? String.format(Locale.FRANCE, "%.4f°, %.4f°", site.getLatitude().doubleValue(), site.getLongitude().doubleValue()) : "—";
        VBox etiquette = Composants.etiquetteSpecimen("Site de reboisement", "N° " + site.getId().substring(0, 8).toUpperCase(Locale.ROOT), List.of(
                new Composants.Champ("Localité", site.getLocalite()),
                new Composants.Champ("Province", site.getProvince()),
                new Composants.Champ("Superficie", site.getSuperficieHectares() != null ? Composants.decimal(site.getSuperficieHectares()) + " ha" : null),
                new Composants.Champ("Coordonnées", coordonnees),
                new Composants.Champ("Responsable", site.getResponsableNom()),
                new Composants.Champ("Essences", essences.isEmpty() ? null : String.join(", ", essences)),
                new Composants.Champ("Campagnes", campagnes.size() + " campagnes · " + Composants.nombre(totalPlants) + " plants")
        ));

        VBox contenu = new VBox(16, new VBox(6, nom, lieu, tete), planche, etiquette);

        if (score != null && score.has("details")) {
            contenu.getChildren().add(blocScore(score));
        }

        Label titreCampagnes = new Label("CAMPAGNES RÉCENTES");
        titreCampagnes.getStyleClass().add("cle-etiquette");
        VBox listeCampagnes = new VBox();
        campagnes.stream().limit(5).forEach(c -> listeCampagnes.getChildren().add(ligneCampagne(c)));
        if (campagnes.isEmpty()) {
            listeCampagnes.getChildren().add(Composants.etatVide("Aucune campagne", "Aucune plantation n'a encore été enregistrée sur ce site."));
        }
        contenu.getChildren().add(new VBox(4, titreCampagnes, listeCampagnes));

        if (voisins != null) {
            VBox listeVoisins = new VBox();
            for (JsonNode v : voisins.path("sites").path("results")) {
                if (v.path("id").asText().equals(site.getId())) {
                    continue;
                }
                listeVoisins.getChildren().add(ligneVoisin(v));
                if (listeVoisins.getChildren().size() >= 4) {
                    break;
                }
            }
            if (!listeVoisins.getChildren().isEmpty()) {
                Label titreVoisins = new Label("SITES À MOINS DE 80 KM");
                titreVoisins.getStyleClass().add("cle-etiquette");
                contenu.getChildren().add(new VBox(4, titreVoisins, listeVoisins));
            }
        }

        Button voirCampagnes = Composants.bouton("Voir les campagnes", Icones.CAMPAGNE, "bouton-primaire");
        voirCampagnes.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(voirCampagnes, Priority.ALWAYS);
        voirCampagnes.setOnAction(e -> Navigation.<CampagnesListController>aller(Navigation.Ecran.CAMPAGNES, c -> c.filtrerParSite(site.getId())));
        HBox actions = new HBox(8, voirCampagnes);
        contenu.getChildren().add(retour);
        if (SessionManager.getInstance().peutAcceder("sites", "edit")) {
            Button modifier = Composants.bouton("Modifier", Icones.MODIFIER, "bouton-secondaire");
            modifier.setOnAction(e -> DialogUtil.<SiteFormController>ouvrirModal("/com/reboisgabon/client/fxml/site-form.fxml", "Modifier le site", controleur -> {
                controleur.definirSiteExistant(site);
                controleur.definirOnSucces(() -> ouvrirSite(site.getId()));
            }));
            actions.getChildren().add(modifier);
        }
        contenu.getChildren().add(actions);

        fiche.getChildren().setAll(contenu);
        defilementFiche.setVvalue(0);
    }

    private VBox jaugeSurvie(BigDecimal taux) {
        Label cle = new Label("TAUX DE SURVIE MOYEN");
        cle.getStyleClass().add("cle-etiquette");
        Label valeur = new Label(taux == null ? "—" : Composants.pourcentage(taux));
        valeur.getStyleClass().add("chiffre-fort");
        if (taux != null) {
            valeur.setStyle("-fx-text-fill: " + Composants.hexSurvie(taux.doubleValue()) + ";");
        }
        double largeur = 190;
        javafx.scene.layout.Pane jauge = new javafx.scene.layout.Pane();
        jauge.setPrefSize(largeur, 26);
        jauge.setMinSize(largeur, 26);
        jauge.setMaxSize(largeur, 26);
        double[][] zones = {{0, 60}, {60, 70}, {70, 80}, {80, 90}, {90, 100}};
        for (double[] z : zones) {
            Rectangle r = new Rectangle(largeur * z[0] / 100, 4, largeur * (z[1] - z[0]) / 100, 8);
            r.setFill(Composants.couleurSurvie(z[0] + 1).deriveColor(0, 0.5, 1.35, 0.45));
            jauge.getChildren().add(r);
        }
        if (taux != null) {
            double t = Math.max(0, Math.min(100, taux.doubleValue()));
            Rectangle plein = new Rectangle(0, 4, largeur * t / 100, 8);
            plein.setFill(Composants.couleurSurvie(t));
            javafx.scene.shape.Polygon curseur = new javafx.scene.shape.Polygon(largeur * t / 100 - 5, 0, largeur * t / 100 + 5, 0, largeur * t / 100, 5);
            curseur.setFill(Color.web("#16291D"));
            jauge.getChildren().addAll(plein, curseur);
        }
        for (int repere : new int[]{50, 70, 90}) {
            javafx.scene.shape.Line trait = new javafx.scene.shape.Line(largeur * repere / 100, 2, largeur * repere / 100, 14);
            trait.setStroke(Color.web("#16291D", 0.55));
            Label l = new Label(String.valueOf(repere));
            l.getStyleClass().add("texte-petit");
            l.setStyle("-fx-font-size: 9.5px;");
            l.setLayoutX(largeur * repere / 100 - 6);
            l.setLayoutY(13);
            jauge.getChildren().addAll(trait, l);
        }
        Label aide = new Label(taux == null ? "Aucun contrôle de terrain" : "moyenne de tous les contrôles");
        aide.getStyleClass().add("texte-petit");
        VBox bloc = new VBox(3, cle, valeur, jauge, aide);
        bloc.setMaxWidth(Region.USE_PREF_SIZE);
        bloc.setPickOnBounds(false);
        return bloc;
    }

    private VBox blocScore(JsonNode score) {
        Label cle = new Label("SCORE ÉCOLOGIQUE");
        cle.getStyleClass().add("cle-etiquette");
        Label valeur = new Label(Composants.decimal(score.path("score_global").asDouble()) + " / 100");
        valeur.getStyleClass().add("chiffre-moyen");
        HBox tete = new HBox(10, valeur);
        tete.setAlignment(Pos.CENTER_LEFT);
        VBox lignes = new VBox(7);
        JsonNode details = score.path("details");
        String[][] criteres = {
                {"survie", "Survie des plants"},
                {"regularite_suivi", "Régularité des contrôles"},
                {"diversite_essences", "Diversité des essences"},
                {"vitalite_statut", "Vitalité du site"}
        };
        for (String[] c : criteres) {
            JsonNode d = details.path(c[0]);
            double s = d.path("score_sur_100").asDouble();
            Label l = new Label(c[1]);
            l.getStyleClass().add("texte-corps");
            Label p = new Label(Composants.decimal(s) + " / 100  ·  poids " + Composants.decimal(d.path("ponderation").asDouble() * 100) + " %");
            p.getStyleClass().add("texte-petit");
            Region espace = new Region();
            HBox.setHgrow(espace, Priority.ALWAYS);
            HBox ligneTexte = new HBox(6, l, espace, p);
            HBox barre = Composants.barreProgression(s / 100.0, 280, Composants.couleurSurvie(s));
            Tooltip.install(barre, new Tooltip(Composants.decimal(s) + " / 100"));
            lignes.getChildren().add(new VBox(3, ligneTexte, barre));
        }
        VBox bloc = new VBox(8, cle, tete, lignes);
        return bloc;
    }

    private HBox ligneCampagne(Campagne c) {
        Node dessin = Illustrations.feuille(Illustrations.especePour(c.getEssenceNom()), 28, Color.web("#1F5136"), Color.web("#E4EFDE"));
        StackPane vignette = new StackPane(dessin);
        vignette.setMinSize(32, 32);
        vignette.setMaxSize(32, 32);
        vignette.setStyle("-fx-background-color: #F4F7F1; -fx-background-radius: 6;");
        Label essence = new Label(c.getEssenceNom() != null ? c.getEssenceNom() : "Essence");
        essence.getStyleClass().add("titre-3");
        Label detail = new Label(Composants.date(c.getDatePlantation()) + "  ·  " + Composants.nombre(c.getNombrePlants()) + " plants");
        detail.getStyleClass().add("texte-petit");
        VBox textes = new VBox(0, essence, detail);
        HBox.setHgrow(textes, Priority.ALWAYS);
        Label survie = new Label(Composants.pourcentage(c.getTauxSurvieMoyen()));
        survie.getStyleClass().add("texte-corps");
        if (c.getTauxSurvieMoyen() != null) {
            survie.setStyle("-fx-text-fill: " + Composants.hexSurvie(c.getTauxSurvieMoyen().doubleValue()) + "; -fx-font-weight: bold;");
        }
        HBox ligne = new HBox(9, vignette, textes, survie);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.getStyleClass().add("ligne-liste");
        ligne.setPadding(new Insets(7, 2, 7, 2));
        return ligne;
    }

    private HBox ligneVoisin(JsonNode v) {
        String id = v.path("id").asText();
        Label nom = new Label(v.path("nom").asText());
        nom.getStyleClass().add("titre-3");
        Label detail = new Label(v.path("localite").asText());
        detail.getStyleClass().add("texte-petit");
        VBox textes = new VBox(0, nom, detail);
        HBox.setHgrow(textes, Priority.ALWAYS);
        textes.setMinWidth(0);
        Label distance = Composants.pastille(Composants.decimal(v.path("distance_km").asDouble()) + " km", "neutre");
        HBox ligne = new HBox(9, Icones.icone(Icones.SITE, 16, Color.web("#46594B")), textes, distance);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.getStyleClass().addAll("ligne-liste", "ligne-liste-cliquable");
        ligne.setPadding(new Insets(7, 2, 7, 2));
        ligne.setOnMouseClicked(e -> ouvrirSite(id));
        return ligne;
    }
}
