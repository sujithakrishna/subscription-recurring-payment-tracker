package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/subscription_tracker";

    private static final String USER = "your_user_name";

    private static final String PASSWORD = "your_password";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void testConnection() {

        try (Connection connection = getConnection()) {

            if (connection != null) {

                System.out.println();
                System.out.println("==============================================");
                System.out.println("        DATABASE CONNECTION SUCCESS");
                System.out.println("==============================================");
                System.out.println("Database : subscription_tracker");
                System.out.println("Status   : CONNECTED");
                System.out.println("==============================================");
            }

        } catch (SQLException e) {

            System.out.println();
            System.out.println("==============================================");
            System.out.println("        DATABASE CONNECTION FAILED");
            System.out.println("==============================================");
            System.out.println("Reason : " + e.getMessage());
            System.out.println("==============================================");
        }
    }
}
