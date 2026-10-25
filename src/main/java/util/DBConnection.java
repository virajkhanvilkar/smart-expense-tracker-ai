package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String HOST = System.getenv("DB_HOST");
    private static final String PORT = System.getenv("DB_PORT");
    private static final String DATABASE = System.getenv("DB_NAME");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static Connection getConnection() {
        if (HOST == null || PORT == null || DATABASE == null
                || USER == null || PASSWORD == null) {
            throw new IllegalStateException(
                "Missing database environment variables.");
        }

        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/"
                + DATABASE + "?sslMode=REQUIRED";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Database connection failed.", e);
        }
    }
}