package kz.eub;

import org.keycloak.component.ComponentModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static kz.eub.DBConstants.*;

public class DataStorage {

    public static Connection getConnection(ComponentModel config) {
        final var driverClass = config.get(DATASOURCE_DRIVER_CLASS.name());

        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("Invalid JDBC driver: %s", e.getMessage()));
        }

        try {
            return DriverManager.getConnection(config.get(DATASOURCE_URL.name()),
                    config.get(DATASOURCE_USERNAME.name()),
                    config.get(DATASOURCE_PASSWORD.name()));
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Cannot get connection: %s", e.getMessage()));
        }
    }
}
