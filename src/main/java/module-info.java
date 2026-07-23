module com.example.batalla_naval {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.batalla_naval to javafx.fxml;
    exports com.example.batalla_naval;
    exports com.example.batalla_naval.Controllers;
    opens com.example.batalla_naval.Controllers to javafx.fxml;
}