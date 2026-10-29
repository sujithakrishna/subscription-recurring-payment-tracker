package subscription;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewSubscriptions {

    public static void viewSubscriptions(User user) {

        Utility.clearScreen();

        Utility.printHeader("MY SUBSCRIPTIONS");

        String sql = """
                SELECT
                    s.subscription_id,
                    p.provider_name,
                    p.plan_name,
                    p.category,
                    s.amount,
                    s.billing_cycle,
                    s.next_billing_date,
                    s.status,
                    s.auto_renew
                FROM subscriptions s
                JOIN subscription_plans p
                    ON s.plan_id = p.plan_id
                WHERE s.user_id = ?
                ORDER BY s.next_billing_date
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
                        "%-5s %-15s %-12s %-12s %-12s %-12s%n",
                        "ID",
                        "PROVIDER",
                        "PLAN",
                        "AMOUNT",
                        "CYCLE",
                        "NEXT BILLING"
                );

                System.out.println(
                        "--------------------------------------------------------------------------------"
                );

                while (resultSet.next()) {

                    found = true;

                    System.out.printf(
                            "%-5d %-15s %-12s ₹%-11.2f %-12s %-12s%n",
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
                                    "billing_cycle"
                            ),
                            resultSet.getDate(
                                    "next_billing_date"
                            )
                    );
                }

                System.out.println(
                        "--------------------------------------------------------------------------------"
                );

                if (!found) {

                    Utility.printInfo(
                            "You don't have any subscriptions yet."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve subscriptions."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }
}