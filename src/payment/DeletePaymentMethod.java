package payment;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeletePaymentMethod {

    public static void delete(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "REMOVE PAYMENT METHOD"
        );

        int methodId =
                InputUtility.readPositiveInt(
                        "Payment Method ID: "
                );

        boolean confirm =
                InputUtility.readYesNo(
                        "Remove this payment method? (Y/N): "
                );

        if (!confirm) {

            Utility.printInfo(
                    "Operation cancelled."
            );

            Utility.pause();

            return;
        }

        String sql = """
                UPDATE payment_methods
                SET status = 'INACTIVE',
                    is_default = FALSE
                WHERE payment_method_id = ?
                AND user_id = ?
                AND status = 'ACTIVE'
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, methodId);
            statement.setInt(2, user.getUserId());

            int rows =
                    statement.executeUpdate();

            if (rows == 1) {

                Utility.printSuccess(
                        "Payment method removed."
                );

            } else {

                Utility.printError(
                        "Payment method not found."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to remove payment method."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }

        Utility.pause();
    }
}