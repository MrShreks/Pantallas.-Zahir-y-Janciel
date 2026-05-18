module com.example.pantallas {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;

    opens com.example.pantallas to javafx.fxml;
    opens com.example.pantallas.ProcesoDeProduccion to javafx.fxml;
    opens com.example.pantallas.ProcesoDeCompras to javafx.fxml;
    opens com.example.pantallas.ProcesoDeInventario to javafx.fxml;
    opens com.example.pantallas.ProcesoDeDistribucion to javafx.fxml;
    opens com.example.pantallas.ProcesoDeMantenimiento to javafx.fxml;
    opens com.example.pantallas.ProcesoDeVenta to javafx.fxml;
    opens com.example.pantallas.MenuPrincipal to javafx.fxml;


    exports com.example.pantallas;
    exports com.example.pantallas.ProcesoDeProduccion;
    exports com.example.pantallas.ProcesoDeCompras;
    exports com.example.pantallas.ProcesoDeInventario;
    exports com.example.pantallas.ProcesoDeDistribucion;
    exports com.example.pantallas.ProcesoDeMantenimiento;
    exports com.example.pantallas.ProcesoDeVenta;
    exports com.example.pantallas.MenuPrincipal;
}