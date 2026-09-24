module com.reboisgabon {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.net.http;

    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.web;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    requires javafx.media;
    requires jdk.jsobject;
    exports com.reboisgabon.client;

    opens com.reboisgabon.client.controllers to javafx.fxml;
    opens com.reboisgabon.client.controllers.sites to javafx.fxml;
    opens com.reboisgabon.client.controllers.campagnes to javafx.fxml;
    opens com.reboisgabon.client.controllers.suivis to javafx.fxml;
    opens com.reboisgabon.client.controllers.objectifs to javafx.fxml;
    opens com.reboisgabon.client.controllers.essences to javafx.fxml;
    opens com.reboisgabon.client.controllers.finances to javafx.fxml;
    opens com.reboisgabon.client.controllers.journal to javafx.fxml;
    opens com.reboisgabon.client.controllers.parametres to javafx.fxml;
    opens com.reboisgabon.client.controllers.notifications to javafx.fxml;
    opens com.reboisgabon.client.controllers.utilisateurs to javafx.fxml;
    opens com.reboisgabon.client.controllers.intelligence to javafx.fxml;
    opens com.reboisgabon.client.ui to javafx.fxml, javafx.web;
    opens com.reboisgabon.client.dto.auth to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.common to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.sites to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.campagnes to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.essences to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.suivis to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.objectifs to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.finances to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.audit to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.compte to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.notifications to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.intelligence to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.dto.utilisateurs to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.session to com.fasterxml.jackson.databind;
}