package org.polyclinic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class db {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/Polyclinic";
    private static final String USER = "postgres";
    private static final String PASSWORD = "27052008den";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
