package com.reboisgabon.client.ui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.function.Function;

public final class Cellules {

    private Cellules() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S> void noeud(TableColumn colonne, Function<S, Node> rendu) {
        colonne.setCellValueFactory(donnees -> new ReadOnlyObjectWrapper<>(((TableColumn.CellDataFeatures) donnees).getValue()));
        colonne.setCellFactory(c -> new TableCell<S, S>() {
            @Override
            protected void updateItem(S element, boolean vide) {
                super.updateItem(element, vide);
                setText(null);
                setGraphic(vide || element == null ? null : rendu.apply(element));
            }
        });
    }

    public static <S> void texte(TableColumn colonne, Function<S, String> principal) {
        Cellules.<S>noeud(colonne, element -> {
            Label l = new Label(valeur(principal.apply(element)));
            l.getStyleClass().add("texte-corps");
            return l;
        });
    }

    public static <S> void droite(TableColumn colonne, Function<S, String> principal) {
        colonne.setStyle("-fx-alignment: CENTER_RIGHT;");
        Cellules.<S>noeud(colonne, element -> {
            Label l = new Label(valeur(principal.apply(element)));
            l.getStyleClass().add("texte-corps");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER_RIGHT);
            return l;
        });
    }

    public static <S> void double_(TableColumn colonne, Function<S, String> principal, Function<S, String> secondaire) {
        Cellules.<S>noeud(colonne, element -> {
            Label haut = new Label(valeur(principal.apply(element)));
            haut.getStyleClass().add("cellule-principale");
            Label bas = new Label(valeur(secondaire.apply(element)));
            bas.getStyleClass().add("texte-petit");
            VBox bloc = new VBox(1, haut, bas);
            bloc.setAlignment(Pos.CENTER_LEFT);
            return bloc;
        });
    }

    public static <S> void scientifique(TableColumn colonne, Function<S, String> principal, Function<S, String> latin) {
        Cellules.<S>noeud(colonne, element -> {
            Label haut = new Label(valeur(principal.apply(element)));
            haut.getStyleClass().add("cellule-principale");
            Label bas = new Label(valeur(latin.apply(element)));
            bas.getStyleClass().add("nom-scientifique");
            bas.setStyle("-fx-font-size: 12px;");
            VBox bloc = new VBox(0, haut, bas);
            bloc.setAlignment(Pos.CENTER_LEFT);
            return bloc;
        });
    }

    private static String valeur(String v) {
        return v == null || v.isBlank() ? "—" : v;
    }

    public static void preparer(TableView<?> table, String titreVide, String texteVide) {
        table.setPlaceholder(Composants.etatVide(titreVide, texteVide));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(52);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void rendu(TableColumn colonne, Function<Object, Node> rendu) {
        colonne.setCellFactory(c -> new TableCell() {
            @Override
            protected void updateItem(Object element, boolean vide) {
                super.updateItem(element, vide);
                setText(null);
                setGraphic(vide || element == null ? null : rendu.apply(element));
            }
        });
    }

    public static Double enNombre(Object valeur) {
        if (valeur == null) {
            return null;
        }
        if (valeur instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(valeur.toString().replace("%", "").replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Label principal(Object valeur) {
        Label l = new Label(valeur == null ? "—" : valeur.toString());
        l.getStyleClass().add("cellule-principale");
        return l;
    }

    public static Label aDroite(Object valeur) {
        Label l = new Label(valeur == null ? "—" : valeur.toString());
        l.getStyleClass().add("texte-corps");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER_RIGHT);
        return l;
    }
}
