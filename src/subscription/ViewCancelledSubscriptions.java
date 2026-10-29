package subscription;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewCancelledSubscriptions {

    public static void viewCancelledSubscriptions(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "CANCELLED SUBSCRIPTIONS"
        );

        String sql = """
                SELECT
                    s.subscription_id,
                    p.provider_name,
                    p.plan_name,
                    p.category,
                    s.amount,
                    s.billing_cycle,
                    s.start_date,
                    s.end_date,
                    s.status
                FROM subscriptions s
                JOIN subscription_plans p
                    ON s.plan_id = p.plan_id
                WHERE s.user_id = ?
                AND s.status = 'CANCELLED'
                ORDER BY s.subscription_id ASC
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
                        "%-5s %-15s %-12s %-16s %-12s %-12s %-12s%n",
                        "ID",
                        "PROVIDER",
                        "PLAN",
                        "CATEGORY",
                        "AMOUNT",
                        "START DATE",
                        "END DATE"
                );

                System.out.println(
                        "--------------------------------------------------------------------------------"
                );

                while (resultSet.next()) {

                    found = true;

                    System.out.printf(
                            "%-5d %-15s %-12s %-16s ₹%-11.2f %-12s %-12s%n",
                            resultSet.getInt(
                                    "subscription_id"
                            ),
                            resultSet.getString(
                                    "provider_name"
                            ),
                            resultSet.getString(
                                    "plan_name"
                            ),
                            resultSet.getString(
                                    "category"
                            ),
                            resultSet.getDouble(
                                    "amount"
                            ),
                            resultSet.getDate(
                                    "start_date"
                            ),
                            resultSet.getDate(
                                    "end_date"
                            )
                    );
                }

                System.out.println(
                        "--------------------------------------------------------------------------------"
                );

                if (!found) {

                    Utility.printInfo(
                            "No cancelled subscriptions found."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve cancelled subscriptions."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }
}