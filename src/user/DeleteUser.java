package user;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteUser {

    public static boolean deleteUser(User user) {

        Utility.clearScreen();

        Utility.printHeader("DEACTIVATE ACCOUNT");

        System.out.println();
        System.out.println(
                "Deactivating your account will prevent future login."
        );

        System.out.println(
                "Your subscription and payment history will be preserved."
        );

        System.out.println();

        boolean confirm =
                InputUtility.readYesNo(
                        "Are you sure you want to continue? (Y/N): "
                );

        if (!confirm) {

            Utility.printInfo(
                    "Account deactivation cancelled."
            );

            Utility.pause();

            return false;
        }

        String sql = """
                UPDATE users
                SET status = 'INACTIVE'
                WHERE user_id = ?
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

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 1) {

                user.setStatus("INACTIVE");

                Utility.printSuccess(
                        "Your account has been deactivated."
                );

                Utility.pause();

                return true;
            }

            Utility.printError(
                    "Unable to deactivate the account."
            );

        } catch (SQLException e) {

            Utility.printError(
                    "Account deactivation failed."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();

        return false;
    }
}