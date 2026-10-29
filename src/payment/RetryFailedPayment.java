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

public class RetryFailedPayment {

    public static void retry(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "RETRY FAILED PAYMENT"
        );

        int paymentId =
                InputUtility.readPositiveInt(
                        "Failed Payment ID: "
                );

        FailureData failure =
                findFailure(
                        paymentId,
                        user.getUserId()
                );

        if (failure == null) {

            Utility.printError(
                    "Retryable payment failure not found."
            );

            Utility.pause();

            return;
        }

        System.out.println();

        System.out.println(
                "Payment ID      : "
                        + paymentId
        );

        System.out.printf(
                "Amount          : ₹%.2f%n",
                failure.amount
        );

        System.out.println(
                "Failure Reason  : "
                        + failure.reason
        );

        System.out.println(
                "Retry Attempt   : "
                        + failure.retryAttempt
        );

        System.out.println();

        boolean retry =
                InputUtility.readYesNo(
                        "Retry this payment now? (Y/N): "
                );

        if (!retry) {

            Utility.printInfo(
                    "Retry cancelled."
            );

            Utility.pause();

            return;
        }

        int methodId =
                InputUtility.readPositiveInt(
                        "Payment Method ID: "
                );

        boolean success =
                InputUtility.readYesNo(
                        "Simulate successful retry? (Y/N): "
                );

        if (success) {

            retrySuccess(
                    user,
                    failure,
                    methodId
            );

        } else {

            retryFailure(
                    user,
                    failure,
                    methodId
            );
        }
    }

    private static FailureData findFailure(
            int paymentId,
            int userId) {

        String sql = """
                SELECT
                    pf.failure_id,
                    pf.payment_id,
                    pf.subscription_id,
                    pf.failure_reason,
                    pf.retry_attempt,
                    pf.failure_status,
                    p.amount
                FROM payment_failures pf
                JOIN payments p
                    ON pf.payment_id =
                       p.payment_id
                WHERE pf.payment_id = ?
                AND p.user_id = ?
                AND pf.failure_status = 'PENDING_RETRY'
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, paymentId);
            statement.setInt(2, userId);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    FailureData data =
                            new FailureData();

                    data.failureId =
                            resultSet.getInt(
                                    "failure_id"
                            );

                    data.paymentId =
                            resultSet.getInt(
                                    "payment_id"
                            );

                    data.subscriptionId =
                            resultSet.getInt(
                                    "subscription_id"
                            );

                    data.reason =
                            resultSet.getString(
                                    "failure_reason"
                            );

                    data.retryAttempt =
                            resultSet.getInt(
                                    "retry_attempt"
                            );

                    data.amount =
                            resultSet.getDouble(
                                    "amount"
                            );

                    return data;
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to find payment failure."
            );
        }

        return null;
    }

    private static void retrySuccess(
            User user,
            FailureData failure,
            int methodId) {

        Connection connection = null;

        String reference =
                "RETRY-"
                        + System.currentTimeMillis();

        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);

            String paymentSql = """
                    UPDATE payments
                    SET payment_status = 'SUCCESS',
                        payment_method_id = ?,
                        retry_count = retry_count + 1,
                        failure_reason = NULL,
                        transaction_reference = ?
                    WHERE payment_id = ?
                    AND user_id = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    paymentSql
                            )
            ) {

                statement.setInt(
                        1,
                        methodId
                );

                statement.setString(
                        2,
                        reference
                );

                statement.setInt(
                        3,
                        failure.paymentId
                );

                statement.setInt(
                        4,
                        user.getUserId()
                );

                statement.executeUpdate();
            }

            String failureSql = """
                    UPDATE payment_failures
                    SET retry_attempt = retry_attempt + 1,
                        failure_status = 'RESOLVED'
                    WHERE failure_id = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    failureSql
                            )
            ) {

                statement.setInt(
                        1,
                        failure.failureId
                );

                statement.executeUpdate();
            }

            String scheduleSql = """
                    UPDATE payment_schedules
                    SET schedule_status = 'COMPLETED'
                    WHERE subscription_id = ?
                    AND schedule_status = 'FAILED'
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    scheduleSql
                            )
            ) {

                statement.setInt(
                        1,
                        failure.subscriptionId
                );

                statement.executeUpdate();
            }

            connection.commit();

            Utility.printSuccess(
                    "Payment retry succeeded."
            );

            System.out.println(
                    "Transaction Reference: "
                            + reference
            );

            NotificationService
                    .createNotification(
                            user.getUserId(),
                            failure.subscriptionId,
                            "PAYMENT_SUCCESS",
                            "Payment retry successful",
                            "Your failed payment was successfully retried."
                    );

        } catch (SQLException e) {

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (SQLException ignored) {
            }

            Utility.printError(
                    "Unable to process payment retry."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );

        } finally {

            closeConnection(
                    connection
            );
        }

        Utility.pause();
    }

    private static void retryFailure(
            User user,
            FailureData failure,
            int methodId) {

        String reason =
                InputUtility.readString(
                        "New Failure Reason: "
                );

        String paymentSql = """
                UPDATE payments
                SET payment_method_id = ?,
                    retry_count = retry_count + 1,
                    failure_reason = ?
                WHERE payment_id = ?
                AND user_id = ?
                """;

        String failureSql = """
                UPDATE payment_failures
                SET retry_attempt = retry_attempt + 1,
                    failure_reason = ?,
                    next_retry_date =
                        DATE_ADD(CURRENT_DATE,
                                 INTERVAL 1 DAY)
                WHERE failure_id = ?
                AND failure_status = 'PENDING_RETRY'
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement paymentStatement =
                        connection.prepareStatement(
                                paymentSql
                        );

                PreparedStatement failureStatement =
                        connection.prepareStatement(
                                failureSql
                        )
        ) {

            paymentStatement.setInt(
                    1,
                    methodId
            );

            paymentStatement.setString(
                    2,
                    reason
            );

            paymentStatement.setInt(
                    3,
                    failure.paymentId
            );

            paymentStatement.setInt(
                    4,
                    user.getUserId()
            );

            paymentStatement.executeUpdate();

            failureStatement.setString(
                    1,
                    reason
            );

            failureStatement.setInt(
                    2,
                    failure.failureId
            );

            failureStatement.executeUpdate();

            Utility.printWarning(
                    "Retry failed again."
            );

            System.out.println(
                    "Another retry has been scheduled."
            );

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to record retry failure."
            );
        }

        Utility.pause();
    }

    private static void closeConnection(
            Connection connection) {

        try {

            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }

        } catch (SQLException ignored) {
        }
    }

    private static class FailureData {

        int failureId;
        int paymentId;
        int subscriptionId;
        int retryAttempt;
        double amount;
        String reason;
    }
}