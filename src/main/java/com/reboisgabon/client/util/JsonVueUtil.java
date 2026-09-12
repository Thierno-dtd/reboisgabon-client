package com.reboisgabon.client.util;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class JsonVueUtil {

    public interface FournisseurPage {
        JsonNode charger(int page) throws Exception;
    }

    private JsonVueUtil() {
    }

    public static VBox construireVue(JsonNode racine) {
        VBox conteneur = new VBox(20);
        if (racine == null || racine.isNull() || racine.isMissingNode()) {
            conteneur.getChildren().add(carteVide("Aucune donnée disponible."));
            return conteneur;
        }
        Iterator<Map.Entry<String, JsonNode>> champs = racine.fields();
        while (champs.hasNext()) {
            Map.Entry<String, JsonNode> entree = champs.next();
            conteneur.getChildren().add(construireSection(entree.getKey(), entree.getValue()));
        }
        return conteneur;
    }

    private static Node construireSection(String cle, JsonNode valeur) {
        VBox section = new VBox(10);
        Label titre = new Label(libelleLisible(cle));
        titre.getStyleClass().add("titre-2");
        if (valeur.isArray()) {
            section.getChildren().addAll(titre, construireTableau(valeur));
        } else if (valeur.isObject()) {
            if (estObjetScalaire(valeur)) {
                section.getChildren().addAll(titre, construireGrilleKpi(valeur));
            } else {
                section.getChildren().addAll(titre, construireVue(valeur));
            }
        } else {
            section.getChildren().add(carteKpi(libelleLisible(cle), formaterValeur(valeur)));
        }
        return section;
    }

    private static boolean estObjetScalaire(JsonNode noeud) {
        Iterator<JsonNode> valeurs = noeud.elements();
        while (valeurs.hasNext()) {
            if (!valeurs.next().isValueNode()) {
                return false;
            }
        }
        return true;
    }

    private static Node construireGrilleKpi(JsonNode noeud) {
        FlowPane grille = new FlowPane(14, 14);
        Iterator<Map.Entry<String, JsonNode>> champs = noeud.fields();
        while (champs.hasNext()) {
            Map.Entry<String, JsonNode> entree = champs.next();
            grille.getChildren().add(carteKpi(libelleLisible(entree.getKey()), formaterValeur(entree.getValue())));
        }
        return grille;
    }

    private static Node carteKpi(String libelle, String valeur) {
        VBox carte = new VBox(6);
        carte.getStyleClass().add("carte-verre");
        carte.setPadding(new Insets(16, 20, 16, 20));
        carte.setMinWidth(180);
        Label libelleNode = new Label(libelle);
        libelleNode.getStyleClass().add("texte-muet");
        Label valeurNode = new Label(valeur);
        valeurNode.getStyleClass().add("titre-1");
        carte.getChildren().addAll(libelleNode, valeurNode);
        return carte;
    }

    private static Node carteVide(String message) {
        VBox carte = new VBox();
        carte.getStyleClass().add("carte-verre");
        carte.setPadding(new Insets(20));
        Label libelle = new Label(message);
        libelle.getStyleClass().add("sous-titre");
        carte.getChildren().add(libelle);
        return carte;
    }

    public static TableView<JsonNode> construireTableau(JsonNode tableau) {
        TableView<JsonNode> table = new TableView<>();
        table.getStyleClass().add("table-generique");
        if (tableau == null || tableau.isMissingNode() || tableau.isEmpty()) {
            return table;
        }
        JsonNode premier = tableau.get(0);
        Iterator<String> cles = premier.fieldNames();
        while (cles.hasNext()) {
            String cle = cles.next();
            TableColumn<JsonNode, String> colonne = new TableColumn<>(libelleLisible(cle));
            colonne.setCellValueFactory(data -> new ReadOnlyStringWrapper(formaterValeur(data.getValue().get(cle))));
            table.getColumns().add(colonne);
        }
        table.getItems().setAll(listeDe(tableau));
        table.setPrefHeight(Math.min(400, 46 + table.getItems().size() * 32));
        return table;
    }

    private static List<JsonNode> listeDe(JsonNode tableau) {
        List<JsonNode> lignes = new ArrayList<>();
        tableau.forEach(lignes::add);
        return lignes;
    }

    public static VBox construireBlocPagine(JsonNode pageInitiale, FournisseurPage fournisseur) {
        VBox bloc = new VBox(14);
        AtomicInteger pageCourante = new AtomicInteger(pageInitiale.path("page").asInt(1));
        TableView<JsonNode> table = construireTableau(pageInitiale.path("results"));
        Label libelleInfo = new Label();
        Button boutonPrecedent = new Button("Précédent");
        Button boutonSuivant = new Button("Suivant");
        boutonPrecedent.getStyleClass().add("bouton-secondaire");
        boutonSuivant.getStyleClass().add("bouton-secondaire");
        HBox barre = new HBox(12, boutonPrecedent, libelleInfo, boutonSuivant);
        barre.getStyleClass().add("barre-pagination");
        mettreAJourEtatPagination(pageInitiale, libelleInfo, boutonPrecedent, boutonSuivant);
        boutonPrecedent.setOnAction(evenement ->
                rechargerPage(fournisseur, pageCourante.decrementAndGet(), table, libelleInfo, boutonPrecedent, boutonSuivant));
        boutonSuivant.setOnAction(evenement ->
                rechargerPage(fournisseur, pageCourante.incrementAndGet(), table, libelleInfo, boutonPrecedent, boutonSuivant));
        bloc.getChildren().addAll(table, barre);
        return bloc;
    }

    private static void rechargerPage(FournisseurPage fournisseur, int page, TableView<JsonNode> table,
                                      Label libelleInfo, Button boutonPrecedent, Button boutonSuivant) {
        new Thread(() -> {
            try {
                JsonNode resultat = fournisseur.charger(page);
                Platform.runLater(() -> {
                    table.getItems().setAll(listeDe(resultat.path("results")));
                    mettreAJourEtatPagination(resultat, libelleInfo, boutonPrecedent, boutonSuivant);
                });
            } catch (Exception e) {
                Platform.runLater(() -> libelleInfo.setText("Erreur de chargement"));
            }
        }).start();
    }

    private static void mettreAJourEtatPagination(JsonNode page, Label libelleInfo, Button boutonPrecedent, Button boutonSuivant) {
        int numeroPage = page.path("page").asInt(1);
        boolean hasNext = page.path("has_next").asBoolean(false);
        boolean hasPrevious = page.path("has_previous").asBoolean(false);
        int total = page.path("count").asInt(0);
        libelleInfo.setText("Page " + numeroPage + " — " + total + " résultats");
        boutonPrecedent.setDisable(!hasPrevious);
        boutonSuivant.setDisable(!hasNext);
    }

    private static String formaterValeur(JsonNode valeur) {
        if (valeur == null || valeur.isNull() || valeur.isMissingNode()) {
            return "—";
        }
        if (valeur.isTextual()) {
            return valeur.asText();
        }
        return valeur.toString();
    }

    private static String libelleLisible(String cle) {
        String[] mots = cle.replace("_", " ").split(" ");
        StringBuilder resultat = new StringBuilder();
        for (String mot : mots) {
            if (mot.isEmpty()) {
                continue;
            }
            resultat.append(Character.toUpperCase(mot.charAt(0))).append(mot.substring(1)).append(" ");
        }
        return resultat.toString().trim();
    }
}