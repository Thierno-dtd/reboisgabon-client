package com.reboisgabon.client.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public final class QrCodeUtil {

    private QrCodeUtil() {
    }

    public static Image genererQrCode(String contenu, int taille) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        var matrice = writer.encode(contenu, BarcodeFormat.QR_CODE, taille, taille);
        BufferedImage imageAwt = MatrixToImageWriter.toBufferedImage(matrice);
        return SwingFXUtils.toFXImage(imageAwt, null);
    }
}