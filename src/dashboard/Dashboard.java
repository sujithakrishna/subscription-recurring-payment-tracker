package dashboard;

import config.DBConnection;
import model.User;
import payment.PaymentScheduleService;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Dashboard {

    public static void show(User user) {

        Utility.clearScreen();

        Utility.printHeader("ACCOUNT OVERVIEW");

        /*
         * Keep payment schedules synchronized with the user's
         * current subscription billing dates before displaying
         * the account overview.
         */
        PaymentScheduleService
                .synchronizeSchedules(user);

        System.out.println();

        showSummary(user);

        System.out.println();

        showNextPayment(user);

        Utility.pause();
    }

    private static void showSummary(User user) {

        String subscriptionSql = """
                SELECT
                    COUNT(*) AS active_subscriptions,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN billing_cycle = 'WEEKLY'
                                THEN amount * 52

                                WHEN billing_cycle = 'MONTHLY'
                                THEN amount * 12

                                WHEN billing_cycle = 'QUARTERLY'
                                THEN amount * 4

                                WHEN billing_cycle = 'HALF_YEARLY'
                                THEN amount * 2

                                WHEN billing_cycle = 'YEARLY'
                                THEN amount

                                ELSE 0
                            END
                        ),
                        0
                    ) AS annual_commitment

                FROM subscriptions
                WHERE user_id = ?
                AND status = 'ACTIVE'
                """;

        String paymentSql = """
                SELECT

                    COALESCE(
                        SUM(
                            CASE
                                WHEN payment_status = 'SUCCESS'
                                THEN 1
                                ELSE 0
                            END
                        ),
                        0
                    ) AS successful_payments,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN payment_status = 'FAILED'
                                THEN 1
                                ELSE 0
                            END
                        ),
                        0
                    ) AS failed_payments

                FROM payments
                WHERE user_id = ?
                """;

        String scheduleSql = """
                SELECT

                    COALESCE(
                        SUM(
                            CASE
                                WHEN schedule_status = 'SCHEDULED'
                                AND scheduled_date >= CURRENT_DATE
                                THEN 1
                                ELSE 0
                            END
                        ),
                        0
                    ) AS upcoming_payments,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN schedule_status = 'SCHEDULED'
                                AND scheduled_date < CURRENT_DATE
                                THEN 1
                                ELSE 0
                            END
                        ),
                        0
                    ) AS overdue_payments

                FROM payment_schedules ps

                JOIN subscriptions s
                    ON ps.subscription_id =
                       s.subscription_id

                WHERE s.user_id = ?
                AND s.status = 'ACTIVE'
                """;

        String notificationSql = """
                SELECT COUNT(*) AS pending_notifications
                FROM notifications
                WHERE user_id = ?
                AND is_read = FALSE
                """;

        try (
                Connection connection =
                        DBConnection.getConnection()
        ) {

            int activeSubscriptions = 0;
            double annualCommitment = 0;

            int successfulPayments = 0;
            int failedPayments = 0;

            int upcomingPayments = 0;
            int overduePayments = 0;

            int pendingNotifications = 0;

            /*
             * Subscription summary
             */

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    subscriptionSql
                            )
            ) {

                statement.setInt(
                        1,
                        user.getUserId()
                );

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {

                    if (resultSet.next()) {

                        activeSubscriptions =
                                resultSet.getInt(
                                        "active_subscriptions"
                                );

                        annualCommitment =
                                resultSet.getDouble(
                                        "annual_commitment"
                                );
                    }
                }
            }

            /*
             * Payment summary
             */

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    paymentSql
                            )
            ) {

                statement.setInt(
                        1,
                        user.getUserId()
                );

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {

                    if (resultSet.next()) {

                        successfulPayments =
                                resultSet.getInt(
                                        "successful_payments"
                                );

                        failedPayments =
                                resultSet.getInt(
                                        "failed_payments"
                                );
                    }
                }
            }

            /*
             * Payment schedule summary
             */

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    scheduleSql
                            )
            ) {

                statement.setInt(
                        1,
                        user.getUserId()
                );

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {

                    if (resultSet.next()) {

                        upcomingPayments =
                                resultSet.getInt(
                                        "upcoming_payments"
                                );

                        overduePayments =
                                resultSet.getInt(
                                        "overdue_payments"
                                );
                    }
                }
            }

            /*
             * Notification summary
             */

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    notificationSql
                            )
            ) {

                statement.setInt(
                        1,
                        user.getUserId()
                );

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {

                    if (resultSet.next()) {

                        pendingNotifications =
                                resultSet.getInt(
                                        "pending_notifications"
                                );
                    }
                }
            }

            double monthlyEquivalent =
                    annualCommitment / 12.0;

            System.out.println(
                    "+------------------------------------------------------------+"
            );

            System.out.println(
                    "|                    ACCOUNT OVERVIEW                       |"
            );

            System.out.println(
                    "+------------------------------------------------------------+"
            );

            System.out.printf(
                    "| Active Subscriptions : %-34d |%n",
                    activeSubscriptions
            );

            System.out.printf(
                    "| Monthly Equivalent   : ₹%-33.2f |%n",
                    monthlyEquivalent
            );

            System.out.printf(
                    "| Annual Commitment    : ₹%-33.2f |%n",
                    annualCommitment
            );

            System.out.printf(
                    "| Upcoming Payments    : %-34d |%n",
                    upcomingPayments
            );

            System.out.printf(
                    "| Overdue Payments     : %-34d |%n",
                    overduePayments
            );

            System.out.printf(
                    "| Successful Payments  : %-34d |%n",
                    successfulPayments
            );

            System.out.printf(
                    "| Failed Payments      : %-34d |%n",
                    failedPayments
            );

            System.out.printf(
                    "| Notifications        : %-34d |%n",
                    pendingNotifications
            );

            System.out.println(
                    "+------------------------------------------------------------+"
            );

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to load account overview."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }
    }

    private static void showNextPayment(User user) {

        String sql = """
                SELECT
                    s.subscription_id,
                    sp.provider_name,
                    sp.plan_name,
                    s.amount,
                    s.billing_cycle,
                    s.next_billing_date
                FROM subscriptions s

                JOIN subscription_plans sp
                    ON s.plan_id = sp.plan_id

                WHERE s.user_id = ?
                AND s.status = 'ACTIVE'
                AND s.next_billing_date IS NOT NULL

                ORDER BY s.next_billing_date ASC

                LIMIT 1
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

                if (!resultSet.next()) {

                    System.out.println();

                    Utility.printInfo(
                            "You currently have no upcoming subscription payment."
                    );

                    return;
                }

                System.out.println();

                System.out.println(
                        "+------------------------------------------------------------+"
                );

                System.out.println(
                        "|                       NEXT PAYMENT                         |"
                );

                System.out.println(
                        "+------------------------------------------------------------+"
                );

                System.out.printf(
                        "| Subscription ID : %-37d |%n",
                        resultSet.getInt(
                                "subscription_id"
                        )
                );

                System.out.printf(
                        "| Provider         : %-37s |%n",
                        resultSet.getString(
                                "provider_name"
                        )
                );

                System.out.printf(
                        "| Plan             : %-37s |%n",
                        resultSet.getString(
                                "plan_name"
                        )
                );

                System.out.printf(
                        "| Amount           : ₹%-36.2f |%n",
                        resultSet.getDouble(
                                "amount"
                        )
                );

                System.out.printf(
                        "| Billing Cycle    : %-37s |%n",
                        resultSet.getString(
                                "billing_cycle"
                        )
                );

                System.out.printf(
                        "| Due Date         : %-37s |%n",
                        resultSet.getDate(
                                "next_billing_date"
                        )
                );

                System.out.println(
                        "+------------------------------------------------------------+"
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to load next payment."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }
    }
}