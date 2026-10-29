package payment;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;
import notification.NotificationService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ProcessPayment {

    public static void process(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "PROCESS PAYMENT"
        );

        PaymentScheduleService
                .synchronizeSchedules(user);

        int scheduleId =
                InputUtility.readPositiveInt(
                        "Schedule ID: "
                );

        PaymentData paymentData =
                findSchedule(
                        scheduleId,
                        user.getUserId()
                );

        if (paymentData == null) {

            Utility.printError(
                    "Payment schedule not found."
            );

            Utility.pause();

            return;
        }

        displayPaymentInformation(
                paymentData
        );

        int methodId =
                selectPaymentMethod(
                        user
                );

        if (methodId == -1) {

            Utility.printError(
                    "No valid payment method selected."
            );

            Utility.pause();

            return;
        }

        System.out.println();

        System.out.println(
                "+----------------------------------------+"
        );

        System.out.println(
                "|         PAYMENT SIMULATION              |"
        );

        System.out.println(
                "+----------------------------------------+"
        );

        System.out.println(
                "| 1 | Successful Payment                 |"
        );

        System.out.println(
                "| 2 | Failed Payment                     |"
        );

        System.out.println(
                "+----------------------------------------+"
        );

        int result =
                InputUtility.readInt(
                        "Choose payment result: "
                );

        if (result != 1 && result != 2) {

            Utility.printError(
                    "Invalid payment result."
            );

            Utility.pause();

            return;
        }

        if (result == 1) {

            processSuccessfulPayment(
                    user,
                    paymentData,
                    methodId
            );

        } else {

            processFailedPayment(
                    user,
                    paymentData,
                    methodId
            );
        }
    }

    private static PaymentData findSchedule(
            int scheduleId,
            int userId) {

        String sql = """
                SELECT
                    ps.schedule_id,
                    ps.subscription_id,
                    ps.amount,
                    ps.scheduled_date,
                    p.provider_name,
                    p.plan_name,
                    s.billing_cycle
                FROM payment_schedules ps
                JOIN subscriptions s
                    ON ps.subscription_id =
                       s.subscription_id
                JOIN subscription_plans p
                    ON s.plan_id = p.plan_id
                WHERE ps.schedule_id = ?
                AND s.user_id = ?
                AND ps.schedule_status = 'SCHEDULED'
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, scheduleId);
            statement.setInt(2, userId);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    PaymentData data =
                            new PaymentData();

                    data.scheduleId =
                            resultSet.getInt(
                                    "schedule_id"
                            );

                    data.subscriptionId =
                            resultSet.getInt(
                                    "subscription_id"
                            );

                    data.amount =
                            resultSet.getDouble(
                                    "amount"
                            );

                    data.scheduledDate =
                            resultSet.getDate(
                                    "scheduled_date"
                            );

                    data.provider =
                            resultSet.getString(
                                    "provider_name"
                            );

                    data.plan =
                            resultSet.getString(
                                    "plan_name"
                            );

                    data.billingCycle =
                            resultSet.getString(
                                    "billing_cycle"
                            );

                    return data;
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to find payment schedule."
            );
        }

        return null;
    }

    private static void displayPaymentInformation(
            PaymentData data) {

        System.out.println();

        System.out.println(
                "+------------------------------------------------------------+"
        );

        System.out.println(
                "|                  PAYMENT INFORMATION                      |"
        );

        System.out.println(
                "+------------------------------------------------------------+"
        );

        System.out.printf(
                "| Schedule ID   : %-39d |%n",
                data.scheduleId
        );

        System.out.printf(
                "| Subscription  : %-39d |%n",
                data.subscriptionId
        );

        System.out.printf(
                "| Provider      : %-39s |%n",
                data.provider
        );

        System.out.printf(
                "| Plan          : %-39s |%n",
                data.plan
        );

        System.out.printf(
                "| Amount        : ₹%-38.2f |%n",
                data.amount
        );

        System.out.printf(
                "| Billing Cycle : %-39s |%n",
                data.billingCycle
        );

        System.out.printf(
                "| Due Date      : %-39s |%n",
                data.scheduledDate
        );

        System.out.println(
                "+------------------------------------------------------------+"
        );
    }

    private static int selectPaymentMethod(
            User user) {

        String sql = """
                SELECT
                    payment_method_id,
                    method_type,
                    method_name,
                    masked_details,
                    is_default
                FROM payment_methods
                WHERE user_id = ?
                AND status = 'ACTIVE'
                ORDER BY is_default DESC,
                         payment_method_id ASC
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

                System.out.println();

                System.out.println(
                        "--------------------------------------------------------------------------"
                );

                System.out.printf(
                        "%-5s %-16s %-18s %-20s %-10s%n",
                        "ID",
                        "TYPE",
                        "NAME",
                        "DETAILS",
                        "DEFAULT"
                );

                System.out.println(
                        "--------------------------------------------------------------------------"
                );

                boolean found = false;

                while (resultSet.next()) {

                    found = true;

                    System.out.printf(
                            "%-5d %-16s %-18s %-20s %-10s%n",
                            resultSet.getInt(
                                    "payment_method_id"
                            ),
                            resultSet.getString(
                                    "method_type"
                            ),
                            resultSet.getString(
                                    "method_name"
                            ),
                            resultSet.getString(
                                    "masked_details"
                            ),
                            resultSet.getBoolean(
                                    "is_default"
                            )
                                    ? "YES"
                                    : "NO"
                    );
                }

                System.out.println(
                        "--------------------------------------------------------------------------"
                );

                if (!found) {

                    Utility.printInfo(
                            "Add a payment method before processing payment."
                    );

                    return -1;
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to load payment methods."
            );

            return -1;
        }

        while (true) {

            int methodId =
                    InputUtility.readPositiveInt(
                            "Payment Method ID: "
                    );

            String validationSql = """
                    SELECT payment_method_id
                    FROM payment_methods
                    WHERE payment_method_id = ?
                    AND user_id = ?
                    AND status = 'ACTIVE'
                    """;

            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(
                                    validationSql
                            )
            ) {

                statement.setInt(
                        1,
                        methodId
                );

                statement.setInt(
                        2,
                        user.getUserId()
                );

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {

                    if (resultSet.next()) {

                        return methodId;
                    }
                }

                Utility.printError(
                        "Invalid payment method ID."
                );

                Utility.printInfo(
                        "Please select an ID from the payment methods listed above."
                );

            } catch (SQLException e) {

                Utility.printError(
                        "Unable to validate payment method."
                );

                return -1;
            }
        }
    }

    private static void processSuccessfulPayment(
            User user,
            PaymentData data,
            int methodId) {

        Connection connection = null;

        String reference =
                generateTransactionReference();

        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);

            String paymentSql = """
                    INSERT INTO payments
                    (
                        subscription_id,
                        user_id,
                        payment_method_id,
                        amount,
                        payment_status,
                        transaction_reference,
                        retry_count
                    )
                    VALUES (?, ?, ?, ?, 'SUCCESS', ?, 0)
                    """;

            int paymentId;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    paymentSql,
                                    PreparedStatement.RETURN_GENERATED_KEYS
                            )
            ) {

                statement.setInt(
                        1,
                        data.subscriptionId
                );

                statement.setInt(
                        2,
                        user.getUserId()
                );

                statement.setInt(
                        3,
                        methodId
                );

                statement.setDouble(
                        4,
                        data.amount
                );

                statement.setString(
                        5,
                        reference
                );

                statement.executeUpdate();

                try (
                        ResultSet keys =
                                statement.getGeneratedKeys()
                ) {

                    keys.next();

                    paymentId =
                            keys.getInt(1);
                }
            }

            updateSchedule(
                    connection,
                    data.scheduleId
            );

            updateNextBillingDate(
                    connection,
                    data.subscriptionId,
                    data.billingCycle,
                    data.scheduledDate
            );

            connection.commit();

            Utility.printSuccess(
                    "Payment completed successfully."
            );

            System.out.println();

            System.out.println(
                    "Payment ID       : "
                            + paymentId
            );

            System.out.println(
                    "Transaction Ref  : "
                            + reference
            );

            System.out.printf(
                    "Amount            : ₹%.2f%n",
                    data.amount
            );

            System.out.println(
                    "Status            : SUCCESS"
            );

            NotificationService
                    .createNotification(
                            user.getUserId(),
                            data.subscriptionId,
                            "PAYMENT_SUCCESS",
                            "Payment successful",
                            "Payment of ₹"
                                    + String.format(
                                    "%.2f",
                                    data.amount
                            )
                                    + " for "
                                    + data.provider
                                    + " was completed successfully."
                    );

        } catch (SQLException e) {

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (SQLException ignored) {
            }

            Utility.printError(
                    "Payment processing failed."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );

        } finally {

            try {

                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }

            } catch (SQLException ignored) {
            }
        }

        Utility.pause();
    }

    private static void processFailedPayment(
            User user,
            PaymentData data,
            int methodId) {

        String reason =
                InputUtility.readString(
                        "Failure Reason: "
                );

        Connection connection = null;

        String reference =
                generateTransactionReference();

        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);

            String paymentSql = """
                    INSERT INTO payments
                    (
                        subscription_id,
                        user_id,
                        payment_method_id,
                        amount,
                        payment_status,
                        transaction_reference,
                        retry_count,
                        failure_reason
                    )
                    VALUES (?, ?, ?, ?, 'FAILED', ?, 0, ?)
                    """;

            int paymentId;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    paymentSql,
                                    PreparedStatement.RETURN_GENERATED_KEYS
                            )
            ) {

                statement.setInt(
                        1,
                        data.subscriptionId
                );

                statement.setInt(
                        2,
                        user.getUserId()
                );

                statement.setInt(
                        3,
                        methodId
                );

                statement.setDouble(
                        4,
                        data.amount
                );

                statement.setString(
                        5,
                        reference
                );

                statement.setString(
                        6,
                        reason
                );

                statement.executeUpdate();

                try (
                        ResultSet keys =
                                statement.getGeneratedKeys()
                ) {

                    keys.next();

                    paymentId =
                            keys.getInt(1);
                }
            }

            updateScheduleFailed(
                    connection,
                    data.scheduleId
            );

            String failureSql = """
                    INSERT INTO payment_failures
                    (
                        payment_id,
                        subscription_id,
                        failure_reason,
                        retry_attempt,
                        next_retry_date,
                        failure_status
                    )
                    VALUES (?, ?, ?, 1,
                            DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY),
                            'PENDING_RETRY')
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    failureSql
                            )
            ) {

                statement.setInt(
                        1,
                        paymentId
                );

                statement.setInt(
                        2,
                        data.subscriptionId
                );

                statement.setString(
                        3,
                        reason
                );

                statement.executeUpdate();
            }

            connection.commit();

            Utility.printWarning(
                    "Payment failed and has been queued for retry."
            );

            System.out.println(
                    "Payment ID      : "
                            + paymentId
            );

            System.out.println(
                    "Failure Reason  : "
                            + reason
            );

            NotificationService
                    .createNotification(
                            user.getUserId(),
                            data.subscriptionId,
                            "PAYMENT_FAILED",
                            "Payment failed",
                            "Payment of ₹"
                                    + String.format(
                                    "%.2f",
                                    data.amount
                            )
                                    + " for "
                                    + data.provider
                                    + " failed. "
                                    + "Retry has been scheduled."
                    );

        } catch (SQLException e) {

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (SQLException ignored) {
            }

            Utility.printError(
                    "Unable to record payment failure."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );

        } finally {

            try {

                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }

            } catch (SQLException ignored) {
            }
        }

        Utility.pause();
    }

    private static void updateSchedule(
            Connection connection,
            int scheduleId)
            throws SQLException {

        String sql = """
                UPDATE payment_schedules
                SET schedule_status = 'COMPLETED'
                WHERE schedule_id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    scheduleId
            );

            statement.executeUpdate();
        }
    }

    private static void updateScheduleFailed(
            Connection connection,
            int scheduleId)
            throws SQLException {

        String sql = """
                UPDATE payment_schedules
                SET schedule_status = 'FAILED'
                WHERE schedule_id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    scheduleId
            );

            statement.executeUpdate();
        }
    }

    private static void updateNextBillingDate(
            Connection connection,
            int subscriptionId,
            String billingCycle,
            java.sql.Date currentDate)
            throws SQLException {

        LocalDateWrapper next =
                calculateNextDate(
                        currentDate.toLocalDate(),
                        billingCycle
                );

        String sql = """
                UPDATE subscriptions
                SET next_billing_date = ?
                WHERE subscription_id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setDate(
                    1,
                    java.sql.Date.valueOf(
                            next.date
                    )
            );

            statement.setInt(
                    2,
                    subscriptionId
            );

            statement.executeUpdate();
        }
    }

    private static LocalDateWrapper calculateNextDate(
            java.time.LocalDate date,
            String cycle) {

        switch (cycle) {

            case "WEEKLY":
                return new LocalDateWrapper(
                        date.plusWeeks(1)
                );

            case "MONTHLY":
                return new LocalDateWrapper(
                        date.plusMonths(1)
                );

            case "QUARTERLY":
                return new LocalDateWrapper(
                        date.plusMonths(3)
                );

            case "HALF_YEARLY":
                return new LocalDateWrapper(
                        date.plusMonths(6)
                );

            case "YEARLY":
                return new LocalDateWrapper(
                        date.plusYears(1)
                );

            default:
                return new LocalDateWrapper(
                        date.plusMonths(1)
                );
        }
    }

    private static String generateTransactionReference() {

        return "TXN-"
                + System.currentTimeMillis();
    }

    private static class LocalDateWrapper {

        java.time.LocalDate date;

        LocalDateWrapper(
                java.time.LocalDate date) {

            this.date = date;
        }
    }

    private static class PaymentData {

        int scheduleId;
        int subscriptionId;
        double amount;
        java.sql.Date scheduledDate;
        String provider;
        String plan;
        String billingCycle;
    }
}