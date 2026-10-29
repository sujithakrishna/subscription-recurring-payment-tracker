package user;

import config.DBConnection;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ForgotPassword {

    public static void resetPassword() {

        Utility.clearScreen();

        Utility.printHeader(
                "RESET PASSWORD"
        );

        System.out.println();

        System.out.println(
                "Verify your account before creating a new password."
        );

        System.out.println();

        String email =
                InputUtility.readEmail(
                        "Email Address : "
                );

        String phone =
                InputUtility.readPhone(
                        "Phone Number  : "
                );

        if (!verifyAccount(email, phone)) {

            Utility.printError(
                    "Account verification failed."
            );

            System.out.println(
                    "Please check your email and phone number."
            );

            Utility.pause();

            return;
        }

        System.out.println();

        Utility.printSuccess(
                "Account verified successfully."
        );

        String newPassword =
                InputUtility.readPassword(
                        "New Password         : "
                );

        String confirmPassword =
                InputUtility.readPassword(
                        "Confirm New Password : "
                );

        if (!newPassword.equals(confirmPassword)) {

            Utility.printError(
                    "Passwords do not match."
            );

            Utility.pause();

            return;
        }

        String sql = """
                UPDATE users
                SET password = ?
                WHERE email = ?
                AND phone = ?
                AND status = 'ACTIVE'
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    newPassword
            );

            statement.setString(
                    2,
                    email
            );

            statement.setString(
                    3,
                    phone
            );

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 1) {

                Utility.printSuccess(
                        "Password updated successfully."
                );

            } else {

                Utility.printError(
                        "Unable to update password."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Database error while resetting password."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }

    private static boolean verifyAccount(
            String email,
            String phone) {

        String sql = """
                SELECT user_id
                FROM users
                WHERE email = ?
                AND phone = ?
                AND status = 'ACTIVE'
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    email
            );

            statement.setString(
                    2,
                    phone
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to verify account."
            );

            return false;
        }
    }
}