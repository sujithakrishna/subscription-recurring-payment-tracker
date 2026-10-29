package reminder;

import config.DBConnection;
import model.User;
import notification.NotificationService;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentReminder {

    public static void generateReminders(
            User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "PAYMENT REMINDERS"
        );

        String sql = """
                SELECT
                    s.subscription_id,
                    p.provider_name,
                    p.plan_name,
                    s.amount,
                    s.next_billing_date
                FROM subscriptions s
                JOIN subscription_plans p
                    ON s.plan_id = p.plan_id
                WHERE s.user_id = ?
                AND s.status = 'ACTIVE'
                AND s.next_billing_date
                    BETWEEN CURRENT_DATE
                    AND DATE_ADD(
                        CURRENT_DATE,
                        INTERVAL 7 DAY
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

                while (resultSet.next()) {

                    found = true;

                    int subscriptionId =
                            resultSet.getInt(
                                    "subscription_id"
                            );

                    String provider =
                            resultSet.getString(
                                    "provider_name"
                            );

                    String plan =
                            resultSet.getString(
                                    "plan_name"
                            );

                    double amount =
                            resultSet.getDouble(
                                    "amount"
                            );

                    String dueDate =
                            resultSet.getDate(
                                    "next_billing_date"
                            ).toString();

                    System.out.println();

                    System.out.println(
                            "+------------------------------------------------------------+"
                    );

                    System.out.println(
                            "|                    PAYMENT REMINDER                       |"
                    );

                    System.out.println(
                            "+------------------------------------------------------------+"
                    );

                    System.out.println(
                            "Provider : " + provider
                    );

                    System.out.println(
                            "Plan     : " + plan
                    );

                    System.out.printf(
                            "Amount   : ₹%.2f%n",
                            amount
                    );

                    System.out.println(
                            "Due Date : " + dueDate
                    );

                    System.out.println(
                            "+------------------------------------------------------------+"
                    );

                    NotificationService
                            .createNotification(
                                    user.getUserId(),
                                    subscriptionId,
                                    "UPCOMING_PAYMENT",
                                    "Upcoming payment",
                                    provider
                                            + " "
                                            + plan
                                            + " payment of ₹"
                                            + String.format(
                                            "%.2f",
                                            amount
                                    )
                                            + " is due on "
                                            + dueDate
                            );
                }

                if (!found) {

                    Utility.printInfo(
                            "No payments are due within the next 7 days."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to generate payment reminders."
            );
        }

        Utility.pause();
    }
}