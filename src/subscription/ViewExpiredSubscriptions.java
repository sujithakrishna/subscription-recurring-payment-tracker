package subscription;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewExpiredSubscriptions {

    public static void viewExpiredSubscriptions(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "EXPIRED SUBSCRIPTIONS"
        );

        String sql = """
                SELECT
                    s.subscription_id,
                    p.provider_name,
                    p.plan_name,
                    s.amount,
                    s.billing_cycle,
                    s.next_billing_date,
                    s.status
                FROM subscriptions s
                JOIN subscription_plans p
                    ON s.plan_id = p.plan_id
                WHERE s.user_id = ?
                AND (
                    s.status = 'EXPIRED'
                    OR (
                        s.status = 'ACTIVE'
                        AND s.next_billing_date < CURRENT_DATE
                    )
                )
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

                while (resultSet.next()) {

                    found = true;

                    System.out.printf(
                            "ID: %d | %s | %s | ₹%.2f | %s | Next Billing: %s%n",
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
                                    "status"
                            ),
                            resultSet.getDate(
                                    "next_billing_date"
                            )
                    );
                }

                if (!found) {

                    Utility.printInfo(
                            "No expired subscriptions found."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve expired subscriptions."
            );
        }

        Utility.pause();
    }
}