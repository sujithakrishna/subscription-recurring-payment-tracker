package payment;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewPaymentMethods {

    public static void viewPaymentMethods(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "MY PAYMENT METHODS"
        );

        String sql = """
                SELECT
                    payment_method_id,
                    method_type,
                    method_name,
                    masked_details,
                    is_default,
                    status
                FROM payment_methods
                WHERE user_id = ?
                ORDER BY payment_method_id ASC
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    user.getUserId()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                boolean found = false;

                System.out.println();

                System.out.println(
                        "--------------------------------------------------------------------------------"
                );

                System.out.printf(
                        "%-5s %-16s %-18s %-20s %-10s %-10s%n",
                        "ID",
                        "TYPE",
                        "NAME",
                        "DETAILS",
                        "DEFAULT",
                        "STATUS"
                );

                System.out.println(
                        "--------------------------------------------------------------------------------"
                );

                while (resultSet.next()) {

                    found = true;

                    System.out.printf(
                            "%-5d %-16s %-18s %-20s %-10s %-10s%n",
                            resultSet.getInt(
                                    "payment_method_id"
                            ),
                            resultSet.getString(
                                    "method_type"
                            ),
                            resultSet.getString(
                                    "method_name"
                            ),
                            resultSet.getString(
                                    "masked_details"
                            ),
                            resultSet.getBoolean(
                                    "is_default"
                            )
                                    ? "YES"
                                    : "NO",
                            resultSet.getString(
                                    "status"
                            )
                    );
                }

                System.out.println(
                        "--------------------------------------------------------------------------------"
                );

                if (!found) {

                    Utility.printInfo(
                            "No payment methods added yet."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve payment methods."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }

        Utility.pause();
    }
}