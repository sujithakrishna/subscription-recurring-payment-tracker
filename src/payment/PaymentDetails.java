package payment;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentDetails {

    public static void show(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "PAYMENT DETAILS"
        );

        int paymentId =
                InputUtility.readPositiveInt(
                        "Payment ID: "
                );

        String sql = """
                SELECT
                    p.payment_id,
                    p.subscription_id,
                    sp.provider_name,
                    sp.plan_name,
                    pm.method_type,
                    pm.method_name,
                    pm.masked_details,
                    p.amount,
                    p.payment_date,
                    p.payment_status,
                    p.transaction_reference,
                    p.retry_count,
                    p.failure_reason
                FROM payments p
                JOIN subscriptions s
                    ON p.subscription_id =
                       s.subscription_id
                JOIN subscription_plans sp
                    ON s.plan_id = sp.plan_id
                LEFT JOIN payment_methods pm
                    ON p.payment_method_id =
                       pm.payment_method_id
                WHERE p.payment_id = ?
                AND p.user_id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, paymentId);
            statement.setInt(2, user.getUserId());

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {

                    Utility.printError(
                            "Payment not found."
                    );

                    Utility.pause();

                    return;
                }

                String method =
                        resultSet.getString(
                                "method_type"
                        );

                if (method == null) {
                    method = "Not available";
                }

                String failure =
                        resultSet.getString(
                                "failure_reason"
                        );

                if (failure == null
                        || failure.isBlank()) {

                    failure = "—";
                }

                System.out.println();

                System.out.println(
                        "+------------------------------------------------------------+"
                );

                System.out.println(
                        "|                    PAYMENT DETAILS                        |"
                );

                System.out.println(
                        "+------------------------------------------------------------+"
                );

                System.out.printf(
                        "| Payment ID       : %-36d |%n",
                        resultSet.getInt(
                                "payment_id"
                        )
                );

                System.out.printf(
                        "| Subscription ID  : %-36d |%n",
                        resultSet.getInt(
                                "subscription_id"
                        )
                );

                System.out.printf(
                        "| Provider         : %-36s |%n",
                        resultSet.getString(
                                "provider_name"
                        )
                );

                System.out.printf(
                        "| Plan             : %-36s |%n",
                        resultSet.getString(
                                "plan_name"
                        )
                );

                System.out.printf(
                        "| Amount           : ₹%-35.2f |%n",
                        resultSet.getDouble(
                                "amount"
                        )
                );

                System.out.printf(
                        "| Payment Method   : %-36s |%n",
                        method
                );

                System.out.printf(
                        "| Payment Status   : %-36s |%n",
                        resultSet.getString(
                                "payment_status"
                        )
                );

                System.out.printf(
                        "| Transaction Ref  : %-36s |%n",
                        resultSet.getString(
                                "transaction_reference"
                        )
                );

                System.out.printf(
                        "| Retry Count      : %-36d |%n",
                        resultSet.getInt(
                                "retry_count"
                        )
                );

                System.out.printf(
                        "| Failure Reason   : %-36s |%n",
                        failure
                );

                System.out.printf(
                        "| Payment Date     : %-36s |%n",
                        resultSet.getTimestamp(
                                "payment_date"
                        )
                );

                System.out.println(
                        "+------------------------------------------------------------+"
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve payment details."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }

        Utility.pause();
    }
}