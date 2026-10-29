package payment;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddPaymentMethod {

    public static void addPaymentMethod(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "ADD PAYMENT METHOD"
        );

        String methodType =
                readMethodType();

        String methodName =
                InputUtility.readString(
                        "Method Name       : "
                );

        String maskedDetails =
                InputUtility.readString(
                        "Masked Details    : "
                );

        boolean makeDefault =
                InputUtility.readYesNo(
                        "Make this the default method? (Y/N): "
                );

        Connection connection = null;

        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);

            if (makeDefault) {

                String resetSql = """
                        UPDATE payment_methods
                        SET is_default = FALSE
                        WHERE user_id = ?
                        AND status = 'ACTIVE'
                        """;

                try (
                        PreparedStatement resetStatement =
                                connection.prepareStatement(
                                        resetSql
                                )
                ) {

                    resetStatement.setInt(
                            1,
                            user.getUserId()
                    );

                    resetStatement.executeUpdate();
                }
            }

            String insertSql = """
                    INSERT INTO payment_methods
                    (
                        user_id,
                        method_type,
                        method_name,
                        masked_details,
                        is_default,
                        status
                    )
                    VALUES (?, ?, ?, ?, ?, 'ACTIVE')
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    insertSql,
                                    PreparedStatement.RETURN_GENERATED_KEYS
                            )
            ) {

                statement.setInt(
                        1,
                        user.getUserId()
                );

                statement.setString(
                        2,
                        methodType
                );

                statement.setString(
                        3,
                        methodName
                );

                statement.setString(
                        4,
                        maskedDetails
                );

                statement.setBoolean(
                        5,
                        makeDefault
                );

                int rows =
                        statement.executeUpdate();

                if (rows == 1) {

                    connection.commit();

                    try (
                            ResultSet keys =
                                    statement.getGeneratedKeys()
                    ) {

                        if (keys.next()) {

                            System.out.println();

                            Utility.printSuccess(
                                    "Payment method added successfully."
                            );

                            System.out.println(
                                    "Payment Method ID : "
                                            + keys.getInt(1)
                            );

                            System.out.println(
                                    "Type               : "
                                            + methodType
                            );

                            System.out.println(
                                    "Name               : "
                                            + methodName
                            );

                            System.out.println(
                                    "Default            : "
                                            + (
                                            makeDefault
                                                    ? "YES"
                                                    : "NO"
                                    )
                            );
                        }
                    }

                } else {

                    connection.rollback();

                    Utility.printError(
                            "Payment method could not be added."
                    );
                }
            }

        } catch (SQLException e) {

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (SQLException ignored) {
            }

            Utility.printError(
                    "Unable to add payment method."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );

        } finally {

            try {

                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }

            } catch (SQLException ignored) {
            }
        }

        Utility.pause();
    }

    private static String readMethodType() {

        while (true) {

            System.out.println();

            System.out.println(
                    "+----------------------------------------+"
            );

            System.out.println(
                    "|           PAYMENT METHOD              |"
            );

            System.out.println(
                    "+----------------------------------------+"
            );

            System.out.println(
                    "| 1 | UPI                                |"
            );

            System.out.println(
                    "| 2 | Credit Card                        |"
            );

            System.out.println(
                    "| 3 | Debit Card                         |"
            );

            System.out.println(
                    "| 4 | Net Banking                        |"
            );

            System.out.println(
                    "| 5 | Wallet                             |"
            );

            System.out.println(
                    "+----------------------------------------+"
            );

            int choice =
                    InputUtility.readInt(
                            "Choose method: "
                    );

            switch (choice) {

                case 1:
                    return "UPI";

                case 2:
                    return "CREDIT_CARD";

                case 3:
                    return "DEBIT_CARD";

                case 4:
                    return "NET_BANKING";

                case 5:
                    return "WALLET";

                default:

                    Utility.printWarning(
                            "Choose a value from 1 to 5."
                    );
            }
        }
    }
}