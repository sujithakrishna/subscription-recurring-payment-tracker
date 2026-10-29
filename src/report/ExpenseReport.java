package report;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseReport {

    public static void show(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "SUBSCRIPTION EXPENSE REPORT"
        );

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
                    ) AS estimated_annual_spend,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN billing_cycle = 'MONTHLY'
                                THEN amount

                                WHEN billing_cycle = 'WEEKLY'
                                THEN amount * 52 / 12

                                WHEN billing_cycle = 'QUARTERLY'
                                THEN amount / 3

                                WHEN billing_cycle = 'HALF_YEARLY'
                                THEN amount / 6

                                WHEN billing_cycle = 'YEARLY'
                                THEN amount / 12

                                ELSE 0
                            END
                        ),
                        0
                    ) AS monthly_equivalent

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
                    ) AS failed_payments,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN payment_status = 'SUCCESS'
                                THEN amount
                                ELSE 0
                            END
                        ),
                        0
                    ) AS total_paid

                FROM payments

                WHERE user_id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection()
        ) {

            int activeSubscriptions = 0;

            double annualSpend = 0;

            double monthlyEquivalent = 0;

            int successfulPayments = 0;

            int failedPayments = 0;

            double totalPaid = 0;

            /*
             * Subscription statistics
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

                        annualSpend =
                                resultSet.getDouble(
                                        "estimated_annual_spend"
                                );

                        monthlyEquivalent =
                                resultSet.getDouble(
                                        "monthly_equivalent"
                                );
                    }
                }
            }

            /*
             * Payment statistics
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

                        totalPaid =
                                resultSet.getDouble(
                                        "total_paid"
                                );
                    }
                }
            }

            /*
             * Display report
             */

            System.out.println();

            System.out.println(
                    "+------------------------------------------------------------+"
            );

            System.out.println(
                    "|               SUBSCRIPTION EXPENSE REPORT                 |"
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
                    "| Estimated Annual     : ₹%-33.2f |%n",
                    annualSpend
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
                    "| Total Amount Paid    : ₹%-33.2f |%n",
                    totalPaid
            );

            System.out.println(
                    "+------------------------------------------------------------+"
            );

            if (activeSubscriptions == 0) {

                Utility.printInfo(
                        "You currently have no active subscriptions."
                );

            } else {

                Utility.printInfo(
                        "Annual spend is estimated using each subscription's billing cycle."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to generate expense report."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }

        Utility.pause();
    }
}