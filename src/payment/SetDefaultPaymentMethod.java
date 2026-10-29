package payment;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SetDefaultPaymentMethod {

    public static void setDefault(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "SET DEFAULT PAYMENT METHOD"
        );

        int methodId =
                InputUtility.readPositiveInt(
                        "Payment Method ID: "
                );

        String verifySql = """
                SELECT payment_method_id
                FROM payment_methods
                WHERE payment_method_id = ?
                AND user_id = ?
                AND status = 'ACTIVE'
                """;

        String resetSql = """
                UPDATE payment_methods
                SET is_default = FALSE
                WHERE user_id = ?
                AND status = 'ACTIVE'
                """;

        String updateSql = """
                UPDATE payment_methods
                SET is_default = TRUE
                WHERE payment_method_id = ?
                AND user_id = ?
                AND status = 'ACTIVE'
                """;

        try (
                Connection connection =
                        DBConnection.getConnection()
        ) {

            try (
                    PreparedStatement verify =
                            connection.prepareStatement(
                                    verifySql
                            )
            ) {

                verify.setInt(1, methodId);
                verify.setInt(2, user.getUserId());

                try (
                        ResultSet resultSet =
                                verify.executeQuery()
                ) {

                    if (!resultSet.next()) {

                        Utility.printError(
                                "Payment method not found."
                        );

                        Utility.pause();

                        return;
                    }
                }
            }

            connection.setAutoCommit(false);

            try (
                    PreparedStatement reset =
                            connection.prepareStatement(
                                    resetSql
                            );

                    PreparedStatement update =
                            connection.prepareStatement(
                                    updateSql
                            )
            ) {

                reset.setInt(
                        1,
                        user.getUserId()
                );

                reset.executeUpdate();

                update.setInt(
                        1,
                        methodId
                );

                update.setInt(
                        2,
                        user.getUserId()
                );

                int rows =
                        update.executeUpdate();

                if (rows == 1) {

                    connection.commit();

                    Utility.printSuccess(
                            "Default payment method updated."
                    );

                } else {

                    connection.rollback();

                    Utility.printError(
                            "Unable to set default method."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to update default payment method."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }

        Utility.pause();
    }
}