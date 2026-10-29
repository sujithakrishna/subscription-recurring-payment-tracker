package payment;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentScheduleService {

    public static void synchronizeSchedules(User user) {

        String subscriptionSql = """
                SELECT
                    subscription_id,
                    amount,
                    next_billing_date
                FROM subscriptions
                WHERE user_id = ?
                AND status = 'ACTIVE'
                AND next_billing_date IS NOT NULL
                """;

        String scheduleExistsSql = """
                SELECT schedule_id
                FROM payment_schedules
                WHERE subscription_id = ?
                AND scheduled_date = ?
                AND schedule_status IN
                    ('SCHEDULED', 'PROCESSING')
                """;

        String insertSql = """
                INSERT INTO payment_schedules
                (
                    subscription_id,
                    scheduled_date,
                    amount,
                    schedule_status
                )
                VALUES (?, ?, ?, 'SCHEDULED')
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement subscriptionStatement =
                        connection.prepareStatement(
                                subscriptionSql
                        );

                PreparedStatement existsStatement =
                        connection.prepareStatement(
                                scheduleExistsSql
                        );

                PreparedStatement insertStatement =
                        connection.prepareStatement(
                                insertSql
                        )
        ) {

            subscriptionStatement.setInt(
                    1,
                    user.getUserId()
            );

            try (
                    ResultSet subscriptions =
                            subscriptionStatement.executeQuery()
            ) {

                while (subscriptions.next()) {

                    int subscriptionId =
                            subscriptions.getInt(
                                    "subscription_id"
                            );

                    java.sql.Date billingDate =
                            subscriptions.getDate(
                                    "next_billing_date"
                            );

                    double amount =
                            subscriptions.getDouble(
                                    "amount"
                            );

                    existsStatement.setInt(
                            1,
                            subscriptionId
                    );

                    existsStatement.setDate(
                            2,
                            billingDate
                    );

                    boolean exists;

                    try (
                            ResultSet schedules =
                                    existsStatement
                                            .executeQuery()
                    ) {

                        exists =
                                schedules.next();
                    }

                    if (!exists) {

                        insertStatement.setInt(
                                1,
                                subscriptionId
                        );

                        insertStatement.setDate(
                                2,
                                billingDate
                        );

                        insertStatement.setDouble(
                                3,
                                amount
                        );

                        insertStatement.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to synchronize payment schedules."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }
    }
}