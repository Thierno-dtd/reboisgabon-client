package com.reboisgabon.client.ui;

import com.reboisgabon.client.util.AlertUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.function.Supplier;

public final class ExportUtil {

    private ExportUtil() {
    }

    public static void pdf(Node source, String nomBase, Supplier<byte[]> fournisseur) {
        telecharger(source, nomBase + "-" + LocalDate.now() + ".pdf", "Document PDF", "*.pdf", fournisseur);
    }

    public static void csv(Node source, String nomBase, Supplier<byte[]> fournisseur) {
        telecharger(source, nomBase + "-" + LocalDate.now() + ".csv", "Fichier CSV", "*.csv", fournisseur);
    }

    public static void excel(Node source, String nomBase, Supplier<byte[]> fournisseur) {
        telecharger(source, nomBase + "-" + LocalDate.now() + ".xlsx", "Classeur Excel", "*.xlsx", fournisseur);
    }

    private static void telecharger(Node source, String nomFichier, String libelle, String motif, Supplier<byte[]> fournisseur) {
        String texteInitial = source instanceof Button b ? b.getText() : null;
        if (source instanceof Button b) {
            b.setDisable(true);
            b.setText("Génération…");
        }
        Thread tache = new Thread(() -> {
            try {
                byte[] contenu = fournisseur.get();
                Platform.runLater(() -> {
                    restaurer(source, texteInitial);
                    enregistrer(source, contenu, nomFichier, libelle, motif);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    restaurer(source, texteInitial);
                    AlertUtil.erreur("Export impossible", "Le serveur n'a pas pu générer ce document. Vérifiez votre connexion puis réessayez.");
                });
            }
        });
        tache.setDaemon(true);
        tache.start();
    }

    private static void restaurer(Node source, String texte) {
        if (source instanceof Button b) {
            b.setDisable(false);
            b.setText(texte);
        }
    }

    private static void enregistrer(Node source, byte[] contenu, String nomFichier, String libelle, String motif) {
        FileChooser selecteur = new FileChooser();
        selecteur.setTitle("Enregistrer l'export");
        selecteur.setInitialFileName(nomFichier);
        selecteur.getExtensionFilters().add(new FileChooser.ExtensionFilter(libelle, motif));
        File dossier = new File(System.getProperty("user.home"), "Documents");
        if (dossier.isDirectory()) {
            selecteur.setInitialDirectory(dossier);
        }
        Window fenetre = source != null && source.getScene() != null ? source.getScene().getWindow() : null;
        File fichier = selecteur.showSaveDialog(fenetre);
        if (fichier == null) {
            return;
        }
        try {
            Files.write(fichier.toPath(), contenu);
        } catch (Exception e) {
            AlertUtil.erreur("Enregistrement impossible", "Le fichier n'a pas pu être écrit à cet emplacement.");
            return;
        }
        boolean ouvrir = AlertUtil.confirmation("Export enregistré", fichier.getName() + " a été enregistré.\nVoulez-vous l'ouvrir maintenant ?");
        if (ouvrir && Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(fichier);
                } catch (Exception ignore) {
                }
            }).start();
        }
    }
}
