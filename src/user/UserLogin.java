package user;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserLogin {

    public static User login() {

        while (true) {

            Utility.clearScreen();

            Utility.printHeader("USER LOGIN");

            System.out.println();

            System.out.println(
                    "  1 | Login"
            );

            System.out.println(
                    "  2 | Forgot Password"
            );

            System.out.println(
                    "  3 | Back"
            );

            System.out.println();

            Utility.printSeparator();

            int choice =
                    InputUtility.readInt(
                            "Select an option: "
                    );

            switch (choice) {

                case 1:

                    User user =
                            performLogin();

                    if (user != null) {

                        return user;
                    }

                    break;

                case 2:

                    ForgotPassword.resetPassword();

                    break;

                case 3:

                    return null;

                default:

                    Utility.printWarning(
                            "Please choose 1, 2 or 3."
                    );

                    Utility.pause();
            }
        }
    }

    private static User performLogin() {

        Utility.clearScreen();

        Utility.printHeader("USER LOGIN");

        System.out.println();

        String email =
                InputUtility.readEmail(
                        "Email Address : "
                );

        String password =
                InputUtility.readPassword(
                        "Password      : "
                );

        String sql = """
                SELECT
                    user_id,
                    full_name,
                    email,
                    phone,
                    password,
                    status
                FROM users
                WHERE email = ?
                AND password = ?
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
                    password
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    User user =
                            new User(
                                    resultSet.getInt(
                                            "user_id"
                                    ),
                                    resultSet.getString(
                                            "full_name"
                                    ),
                                    resultSet.getString(
                                            "email"
                                    ),
                                    resultSet.getString(
                                            "phone"
                                    ),
                                    resultSet.getString(
                                            "password"
                                    ),
                                    resultSet.getString(
                                            "status"
                                    )
                            );

                    Utility.printSuccess(
                            "Login successful."
                    );

                    System.out.println();

                    System.out.println(
                            "Welcome back, "
                                    + user.getFullName()
                                    + "!"
                    );

                    Utility.pause();

                    return user;
                }
            }

            Utility.printError(
                    "Invalid email or password."
            );

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to process login."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();

        return null;
    }
}