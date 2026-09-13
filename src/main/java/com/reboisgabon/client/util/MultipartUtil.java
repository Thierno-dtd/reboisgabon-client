package com.reboisgabon.client.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public final class MultipartUtil {

    private MultipartUtil() {
    }

    public static byte[] construire(Map<String, String> champs, String nomChampFichier, File fichier, String boundary) throws IOException {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        String saut = "\r\n";
        for (Map.Entry<String, String> champ : champs.entrySet()) {
            if (champ.getValue() == null) {
                continue;
            }
            sortie.write(("--" + boundary + saut).getBytes(StandardCharsets.UTF_8));
            sortie.write(("Content-Disposition: form-data; name=\"" + champ.getKey() + "\"" + saut + saut).getBytes(StandardCharsets.UTF_8));
            sortie.write((champ.getValue() + saut).getBytes(StandardCharsets.UTF_8));
        }
        sortie.write(("--" + boundary + saut).getBytes(StandardCharsets.UTF_8));
        sortie.write(("Content-Disposition: form-data; name=\"" + nomChampFichier + "\"; filename=\"" + fichier.getName() + "\"" + saut).getBytes(StandardCharsets.UTF_8));
        sortie.write(("Content-Type: " + typeMime(fichier) + saut + saut).getBytes(StandardCharsets.UTF_8));
        sortie.write(Files.readAllBytes(fichier.toPath()));
        sortie.write(saut.getBytes(StandardCharsets.UTF_8));
        sortie.write(("--" + boundary + "--" + saut).getBytes(StandardCharsets.UTF_8));
        return sortie.toByteArray();
    }

    private static String typeMime(File fichier) {
        String nom = fichier.getName().toLowerCase();
        if (nom.endsWith(".png")) {
            return "image/png";
        }
        if (nom.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }
}