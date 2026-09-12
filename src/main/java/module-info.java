module com.reboisgabon {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.net.http;

    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    exports com.reboisgabon.client;

    opens com.reboisgabon.client.controllers to javafx.fxml;
    opens com.reboisgabon.client.dto.auth to com.fasterxml.jackson.databind;
    opens com.reboisgabon.client.session to com.fasterxml.jackson.databind;
}