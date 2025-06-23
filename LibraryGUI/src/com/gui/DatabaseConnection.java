package com.gui;

import java.sql.*;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_schema";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Hashim@2004";

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }
}
