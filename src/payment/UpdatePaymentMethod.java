package payment;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdatePaymentMethod {

    public static void update(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "UPDATE PAYMENT METHOD"
        );

        int methodId =
                InputUtility.readPositiveInt(
                        "Payment Method ID: "
                );

        String methodName =
                InputUtility.readString(
                        "New Method Name: "
                );

        String maskedDetails =
                InputUtility.readString(
                        "New Masked Details: "
                );

        String sql = """
                UPDATE payment_methods
                SET method_name = ?,
                    masked_details = ?
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

            statement.setString(1, methodName);
            statement.setString(2, maskedDetails);
            statement.setInt(3, methodId);
            statement.setInt(4, user.getUserId());

            int rows =
                    statement.executeUpdate();

            if (rows == 1) {

                Utility.printSuccess(
                        "Payment method updated successfully."
                );

            } else {

                Utility.printError(
                        "Payment method not found."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to update payment method."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }

        Utility.pause();
    }
}