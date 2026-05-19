package me.sekc.clans;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    Connection connection;

    DatabaseConnection(String databasePath) throws Exception {
        Clans.log("Loading SQLite database at \"" + databasePath + "\" ...");

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(databasePath);

        if (connection != null) {
            var meta = connection.getMetaData();
            Clans.log("Database driver: " + meta.getDriverName());
        }

        connection.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS clans ("
                        + "   name           VARCHAR(32) NOT NULL,"
                        + "   description    VARCHAR(512)"
                        + ")"
        );

        connection.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS players ("
                        + "   name           VARCHAR(256) NOT NULL,"
                        + "   experience     INTEGER"
                        + ")"
        );

    }
}
