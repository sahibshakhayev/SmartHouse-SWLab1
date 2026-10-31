module hu.smarthouse.smarthouse {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires static lombok;
    requires java.net.http;
    requires org.json;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;

    opens hu.smarthouse.smarthouse.ui to javafx.graphics, javafx.fxml;
    exports hu.smarthouse.smarthouse.ui;

    exports hu.smarthouse.smarthouse.core;
    exports hu.smarthouse.smarthouse.model;
}