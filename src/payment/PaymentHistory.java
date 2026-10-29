package payment;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentHistory {

    public static void show(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "PAYMENT HISTORY"
        );

        String sql = """
                SELECT
                    p.payment_id,
                    p.subscription_id,
                    sp.provider_name,
                    sp.plan_name,
                    p.amount,
                    p.payment_date,
                    p.payment_status,
                    p.transaction_reference,
                    p.retry_count
                FROM payments p

                JOIN subscriptions s
                    ON p.subscription_id =
                       s.subscription_id

                JOIN subscription_plans sp
                    ON s.plan_id =
                       sp.plan_id

                WHERE p.user_id = ?

                ORDER BY p.payment_id DESC
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
                        "------------------------------------------------------------------------------------------------"
                );

                System.out.printf(
                        "%-11s %-16s %-16s %-15s %-13s %-12s %-8s%n",
                        "PAYMENT ID",
                        "SUBSCRIPTION ID",
                        "PROVIDER",
                        "PLAN",
                        "AMOUNT",
                        "STATUS",
                        "RETRIES"
                );

                System.out.println(
                        "------------------------------------------------------------------------------------------------"
                );

                while (resultSet.next()) {

                    found = true;

                    System.out.printf(
                            "%-11d %-16d %-16s %-15s ₹%-12.2f %-12s %-8d%n",

                            resultSet.getInt(
                                    "payment_id"
                            ),

                            resultSet.getInt(
                                    "subscription_id"
                            ),

                            resultSet.getString(
                                    "provider_name"
                            ),

                            resultSet.getString(
                                    "plan_name"
                            ),

                            resultSet.getDouble(
                                    "amount"
                            ),

                            resultSet.getString(
                                    "payment_status"
                            ),

                            resultSet.getInt(
                                    "retry_count"
                            )
                    );

                    System.out.println(
                            "             Transaction Reference : "
                                    + resultSet.getString(
                                    "transaction_reference"
                            )
                    );

                    System.out.println(
                            "             Payment Date          : "
                                    + resultSet.getTimestamp(
                                    "payment_date"
                            )
                    );

                    System.out.println(
                            "------------------------------------------------------------------------------------------------"
                    );
                }

                if (!found) {

                    Utility.printInfo(
                            "No payment history available."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve payment history."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }

        Utility.pause();
    }
}