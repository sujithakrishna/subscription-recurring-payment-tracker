package subscription;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewActiveSubscriptions {

    public static void viewActiveSubscriptions(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "ACTIVE SUBSCRIPTIONS"
        );

        String sql = """
                SELECT
                    s.subscription_id,
                    p.provider_name,
                    p.plan_name,
                    s.amount,
                    s.billing_cycle,
                    s.next_billing_date,
                    s.auto_renew
                FROM subscriptions s
                JOIN subscription_plans p
                    ON s.plan_id = p.plan_id
                WHERE s.user_id = ?
                AND s.status = 'ACTIVE'
                ORDER BY s.next_billing_date
                """;

        display(user, sql);
    }

    private static void display(
            User user,
            String sql) {

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
                        "--------------------------------------------------------------------------"
                );

                System.out.printf(
                        "%-5s %-15s %-12s %-12s %-12s %-10s%n",
                        "ID",
                        "PROVIDER",
                        "PLAN",
                        "AMOUNT",
                        "CYCLE",
                        "RENEW"
                );

                System.out.println(
                        "--------------------------------------------------------------------------"
                );

                while (resultSet.next()) {

                    found = true;

                    System.out.printf(
                            "%-5d %-15s %-12s ₹%-11.2f %-12s %-10s%n",
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
                            resultSet.getBoolean(
                                    "auto_renew"
                            )
                                    ? "ON"
                                    : "OFF"
                    );
                }

                System.out.println(
                        "--------------------------------------------------------------------------"
                );

                if (!found) {

                    Utility.printInfo(
                            "No active subscriptions found."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve active subscriptions."
            );
        }

        Utility.pause();
    }
}