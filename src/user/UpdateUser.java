package user;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateUser {

    public static boolean updateUser(User user) {

        Utility.clearScreen();

        Utility.printHeader("UPDATE MY PROFILE");

        System.out.println();
        System.out.println("Leave the current values unchanged where applicable.");
        System.out.println();

        String newName =
                InputUtility.readString(
                        "New Full Name : "
                );

        String newPhone =
                InputUtility.readPhone(
                        "New Phone     : "
                );

        String sql = """
                UPDATE users
                SET full_name = ?,
                    phone = ?
                WHERE user_id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, newName);
            statement.setString(2, newPhone);
            statement.setInt(3, user.getUserId());

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 1) {

                user.setFullName(newName);
                user.setPhone(newPhone);

                Utility.printSuccess(
                        "Profile updated successfully."
                );

                Utility.pause();

                return true;
            }

            Utility.printError(
                    "No profile was updated."
            );

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to update profile."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();

        return false;
    }
}