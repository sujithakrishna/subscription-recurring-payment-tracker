package user;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRegistration {

    public static void registerUser() {

        Utility.clearScreen();

        Utility.printHeader("CREATE YOUR ACCOUNT");

        System.out.println();
        System.out.println("Create your account to start tracking");
        System.out.println("subscriptions and recurring payments.");
        System.out.println();

        String fullName =
                InputUtility.readString(
                        "Full Name        : "
                );

        String email =
                InputUtility.readEmail(
                        "Email Address    : "
                );

        String phone =
                InputUtility.readPhone(
                        "Phone Number     : "
                );

        String password =
                InputUtility.readPassword(
                        "Password         : "
                );

        String confirmPassword =
                InputUtility.readPassword(
                        "Confirm Password : "
                );

        if (!password.equals(confirmPassword)) {

            Utility.printError(
                    "Passwords do not match."
            );

            Utility.pause();

            return;
        }

        if (emailExists(email)) {

            Utility.printError(
                    "An account already exists with this email."
            );

            Utility.pause();

            return;
        }

        User user =
                new User(
                        fullName,
                        email,
                        phone,
                        password
                );

        String sql = """
                INSERT INTO users
                (full_name, email, phone, password)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                PreparedStatement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setString(
                    1,
                    user.getFullName()
            );

            statement.setString(
                    2,
                    user.getEmail()
            );

            statement.setString(
                    3,
                    user.getPhone()
            );

            statement.setString(
                    4,
                    user.getPassword()
            );

            int rowsInserted =
                    statement.executeUpdate();

            if (rowsInserted == 1) {

                try (
                        ResultSet generatedKeys =
                                statement.getGeneratedKeys()
                ) {

                    if (generatedKeys.next()) {

                        int userId =
                                generatedKeys.getInt(1);

                        System.out.println();

                        System.out.println(
                                "+------------------------------------------------------------+"
                        );

                        System.out.println(
                                "|              ACCOUNT CREATED SUCCESSFULLY                 |"
                        );

                        System.out.println(
                                "+------------------------------------------------------------+"
                        );

                        System.out.printf(
                                "| User ID : %-48d |%n",
                                userId
                        );

                        System.out.printf(
                                "| Name    : %-48s |%n",
                                fullName
                        );

                        System.out.printf(
                                "| Email   : %-48s |%n",
                                email
                        );

                        System.out.printf(
                                "| Phone   : %-48s |%n",
                                phone
                        );

                        System.out.println(
                                "| Status  : ACTIVE                                           |"
                        );

                        System.out.println(
                                "+------------------------------------------------------------+"
                        );
                    }
                }

                Utility.printSuccess(
                        "Your account is ready."
                );

            } else {

                Utility.printError(
                        "Registration could not be completed."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to create the account."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }

    private static boolean emailExists(String email) {

        String sql =
                "SELECT user_id FROM users WHERE email = ?";

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

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to verify the email address."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );

            return true;
        }
    }
}