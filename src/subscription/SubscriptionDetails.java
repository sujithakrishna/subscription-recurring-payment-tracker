package subscription;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubscriptionDetails {

    public static void showDetails(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "SUBSCRIPTION DETAILS"
        );

        int subscriptionId =
                InputUtility.readPositiveInt(
                        "Subscription ID: "
                );

        String sql = """
                SELECT
                    s.subscription_id,
                    p.provider_name,
                    p.plan_name,
                    p.category,
                    p.description,
                    s.amount,
                    s.billing_cycle,
                    s.start_date,
                    s.next_billing_date,
                    s.end_date,
                    s.status,
                    s.auto_renew
                FROM subscriptions s
                JOIN subscription_plans p
                    ON s.plan_id = p.plan_id
                WHERE s.subscription_id = ?
                AND s.user_id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, subscriptionId);
            statement.setInt(2, user.getUserId());

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {

                    Utility.printError(
                            "Subscription not found."
                    );

                    Utility.pause();

                    return;
                }

                String endDate =
                        resultSet.getDate("end_date") == null
                                ? "—"
                                : resultSet
                                    .getDate("end_date")
                                    .toString();

                String status =
                        resultSet.getString("status");

                if (status.equals("ACTIVE")
                        || status.equals("PAUSED")) {

                    endDate = "Not ended";
                }

                System.out.println();

                System.out.println(
                        "+------------------------------------------------------------+"
                );

                System.out.println(
                        "|                  SUBSCRIPTION DETAILS                     |"
                );

                System.out.println(
                        "+------------------------------------------------------------+"
                );

                System.out.printf(
                        "| ID             : %-39d |%n",
                        resultSet.getInt(
                                "subscription_id"
                        )
                );

                System.out.printf(
                        "| Provider       : %-39s |%n",
                        resultSet.getString(
                                "provider_name"
                        )
                );

                System.out.printf(
                        "| Plan           : %-39s |%n",
                        resultSet.getString(
                                "plan_name"
                        )
                );

                System.out.printf(
                        "| Category       : %-39s |%n",
                        resultSet.getString(
                                "category"
                        )
                );

                System.out.printf(
                        "| Amount         : ₹%-38.2f |%n",
                        resultSet.getDouble(
                                "amount"
                        )
                );

                System.out.printf(
                        "| Billing Cycle  : %-39s |%n",
                        resultSet.getString(
                                "billing_cycle"
                        )
                );

                System.out.printf(
                        "| Start Date     : %-39s |%n",
                        resultSet.getDate(
                                "start_date"
                        )
                );

                System.out.printf(
                        "| Next Billing   : %-39s |%n",
                        resultSet.getDate(
                                "next_billing_date"
                        )
                );

                System.out.printf(
                        "| End Date       : %-39s |%n",
                        endDate
                );

                System.out.printf(
                        "| Status         : %-39s |%n",
                        status
                );

                System.out.printf(
                        "| Auto Renewal   : %-39s |%n",
                        resultSet.getBoolean(
                                "auto_renew"
                        )
                                ? "ENABLED"
                                : "DISABLED"
                );

                System.out.println(
                        "+------------------------------------------------------------+"
                );

                System.out.println();
                System.out.println("Description:");
                System.out.println(
                        resultSet.getString("description")
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve subscription details."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }
}