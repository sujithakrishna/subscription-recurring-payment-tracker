package user;

import config.DBConnection;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DisplayUsers {

    public static void displayUsers() {

        Utility.clearScreen();

        Utility.printHeader("REGISTERED USERS");

        String sql = """
                SELECT
                    user_id,
                    full_name,
                    email,
                    phone,
                    status
                FROM users
                ORDER BY user_id
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            boolean found = false;

            System.out.println();

            System.out.println(
                    "--------------------------------------------------------------------------"
            );

            System.out.printf(
                    "%-8s %-20s %-28s %-14s %-10s%n",
                    "ID",
                    "NAME",
                    "EMAIL",
                    "PHONE",
                    "STATUS"
            );

            System.out.println(
                    "--------------------------------------------------------------------------"
            );

            while (resultSet.next()) {

                found = true;

                System.out.printf(
                        "%-8d %-20s %-28s %-14s %-10s%n",
                        resultSet.getInt("user_id"),
                        resultSet.getString("full_name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("status")
                );
            }

            System.out.println(
                    "--------------------------------------------------------------------------"
            );

            if (!found) {

                Utility.printInfo(
                        "No users are currently registered."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve users."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }
}