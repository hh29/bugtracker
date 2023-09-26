module com.example.bugtracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires junit;

    exports com.example.bugtracker.Controller.DialogController;
    exports com.example.bugtracker.Model.DAO;
    opens com.example.bugtracker.Model.DAO to javafx.fxml;
    exports com.example.bugtracker.DBConnection;
    opens com.example.bugtracker.DBConnection to javafx.fxml;
    exports com.example.bugtracker.Controller.Chat;
    opens com.example.bugtracker.Controller.Chat to javafx.fxml;
    exports com.example.bugtracker.Controller.Admin;
    opens com.example.bugtracker.Controller.Admin to javafx.fxml;
    exports com.example.bugtracker.Controller.Developer;
    opens com.example.bugtracker.Controller.Developer to javafx.fxml;
    exports com.example.bugtracker.Controller.TechSupport;
    opens com.example.bugtracker.Controller.TechSupport to javafx.fxml;
    exports com.example.bugtracker.Controller.ButtonHandler;
    opens com.example.bugtracker.Controller.ButtonHandler to javafx.fxml;
    exports com.example.bugtracker.Main;
    opens com.example.bugtracker.Main to javafx.fxml;
    opens com.example.bugtracker.Controller.DialogController to javafx.fxml;
    exports com.example.bugtracker.Controller.Login;
    opens com.example.bugtracker.Controller.Login to javafx.fxml;
    exports com.example.bugtracker.Controller.Signup;
    opens com.example.bugtracker.Controller.Signup to javafx.fxml;
    exports com.example.bugtracker.Model.Login;
    opens com.example.bugtracker.Model.Login to javafx.fxml;
    exports com.example.bugtracker.Model.Signup;
    opens com.example.bugtracker.Model.Signup to javafx.fxml;
    exports com.example.bugtracker.Model.Entity;
    opens com.example.bugtracker.Model.Entity to javafx.fxml;
    exports com.example.bugtracker.Controller.ProjectManager;
    opens com.example.bugtracker.Controller.ProjectManager to javafx.fxml;


}
