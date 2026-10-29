package subscription;

import config.DBConnection;
import model.User;
import utility.DateUtility;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class UpdateSubscription {

    public static void updateSubscription(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "UPDATE SUBSCRIPTION"
        );

        int subscriptionId =
                InputUtility.readPositiveInt(
                        "Subscription ID: "
                );

        if (!subscriptionBelongsToUser(
                subscriptionId,
                user.getUserId())) {

            Utility.printError(
                    "Subscription not found or cannot be updated."
            );

            Utility.pause();

            return;
        }

        while (true) {

            System.out.println();

            System.out.println(
                    "+------------------------------------------------------------+"
            );

            System.out.println(
                    "|                  UPDATE OPTIONS                            |"
            );

            System.out.println(
                    "+------------------------------------------------------------+"
            );

            System.out.println(
                    "| 1 | Update Amount                                          |"
            );

            System.out.println(
                    "| 2 | Update Billing Cycle                                   |"
            );

            System.out.println(
                    "| 3 | Update Auto Renewal                                    |"
            );

            System.out.println(
                    "| 4 | Update All                                             |"
            );

            System.out.println(
                    "| 5 | Go Back                                                |"
            );

            System.out.println(
                    "+------------------------------------------------------------+"
            );

            int choice =
                    InputUtility.readInt(
                            "Select an option: "
                    );

            switch (choice) {

                case 1:
                    updateAmount(
                            subscriptionId,
                            user.getUserId()
                    );
                    return;

                case 2:
                    updateBillingCycle(
                            subscriptionId,
                            user.getUserId()
                    );
                    return;

                case 3:
                    updateAutoRenew(
                            subscriptionId,
                            user.getUserId()
                    );
                    return;

                case 4:
                    updateAll(
                            subscriptionId,
                            user.getUserId()
                    );
                    return;

                case 5:
                    return;

                default:
                    Utility.printWarning(
                            "Please choose 1 - 5."
                    );
            }
        }
    }

    private static void updateAmount(
            int subscriptionId,
            int userId) {

        double amount =
                InputUtility.readPositiveDouble(
                        "New Amount: ₹"
                );

        String sql = """
                UPDATE subscriptions
                SET amount = ?
                WHERE subscription_id = ?
                AND user_id = ?
                AND status IN ('ACTIVE', 'PAUSED')
                """;

        executeUpdate(
                sql,
                amount,
                subscriptionId,
                userId,
                "Amount updated successfully."
        );
    }

    private static void updateBillingCycle(
            int subscriptionId,
            int userId) {

        String cycle =
                readBillingCycle();

        LocalDate nextBillingDate =
                DateUtility.calculateNextBillingDate(
                        LocalDate.now(),
                        cycle
                );

        String sql = """
                UPDATE subscriptions
                SET billing_cycle = ?,
                    next_billing_date = ?
                WHERE subscription_id = ?
                AND user_id = ?
                AND status IN ('ACTIVE', 'PAUSED')
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, cycle);

            statement.setDate(
                    2,
                    java.sql.Date.valueOf(
                            nextBillingDate
                    )
            );

            statement.setInt(3, subscriptionId);
            statement.setInt(4, userId);

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 1) {

                Utility.printSuccess(
                        "Billing cycle updated successfully."
                );

                System.out.println(
                        "Next billing date: "
                                + nextBillingDate
                );

            } else {

                Utility.printError(
                        "Subscription could not be updated."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to update billing cycle."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }

    private static void updateAutoRenew(
            int subscriptionId,
            int userId) {

        boolean autoRenew =
                InputUtility.readYesNo(
                        "Enable auto-renewal? (Y/N): "
                );

        String sql = """
                UPDATE subscriptions
                SET auto_renew = ?
                WHERE subscription_id = ?
                AND user_id = ?
                AND status IN ('ACTIVE', 'PAUSED')
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setBoolean(1, autoRenew);
            statement.setInt(2, subscriptionId);
            statement.setInt(3, userId);

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 1) {

                Utility.printSuccess(
                        "Auto-renewal preference updated."
                );

            } else {

                Utility.printError(
                        "Subscription could not be updated."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to update auto-renewal."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }

    private static void updateAll(
            int subscriptionId,
            int userId) {

        double amount =
                InputUtility.readPositiveDouble(
                        "New Amount: ₹"
                );

        String cycle =
                readBillingCycle();

        boolean autoRenew =
                InputUtility.readYesNo(
                        "Enable auto-renewal? (Y/N): "
                );

        LocalDate nextBillingDate =
                DateUtility.calculateNextBillingDate(
                        LocalDate.now(),
                        cycle
                );

        String sql = """
                UPDATE subscriptions
                SET amount = ?,
                    billing_cycle = ?,
                    next_billing_date = ?,
                    auto_renew = ?
                WHERE subscription_id = ?
                AND user_id = ?
                AND status IN ('ACTIVE', 'PAUSED')
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setDouble(1, amount);
            statement.setString(2, cycle);

            statement.setDate(
                    3,
                    java.sql.Date.valueOf(
                            nextBillingDate
                    )
            );

            statement.setBoolean(4, autoRenew);
            statement.setInt(5, subscriptionId);
            statement.setInt(6, userId);

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 1) {

                Utility.printSuccess(
                        "Subscription updated successfully."
                );

                System.out.println(
                        "Next billing date: "
                                + nextBillingDate
                );

            } else {

                Utility.printError(
                        "Subscription could not be updated."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to update subscription."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }

    private static void executeUpdate(
            String sql,
            double amount,
            int subscriptionId,
            int userId,
            String successMessage) {

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setDouble(1, amount);
            statement.setInt(2, subscriptionId);
            statement.setInt(3, userId);

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 1) {

                Utility.printSuccess(
                        successMessage
                );

            } else {

                Utility.printError(
                        "Subscription could not be updated."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to update subscription."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }

    private static boolean subscriptionBelongsToUser(
            int subscriptionId,
            int userId) {

        String sql = """
                SELECT subscription_id
                FROM subscriptions
                WHERE subscription_id = ?
                AND user_id = ?
                AND status IN ('ACTIVE', 'PAUSED')
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, subscriptionId);
            statement.setInt(2, userId);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (SQLException e) {

            return false;
        }
    }

    private static String readBillingCycle() {

        while (true) {

            System.out.println();
            System.out.println("1 | Weekly");
            System.out.println("2 | Monthly");
            System.out.println("3 | Quarterly");
            System.out.println("4 | Half-Yearly");
            System.out.println("5 | Yearly");

            int choice =
                    InputUtility.readInt(
                            "Choose billing cycle: "
                    );

            switch (choice) {

                case 1:
                    return "WEEKLY";

                case 2:
                    return "MONTHLY";

                case 3:
                    return "QUARTERLY";

                case 4:
                    return "HALF_YEARLY";

                case 5:
                    return "YEARLY";

                default:
                    Utility.printWarning(
                            "Choose a value from 1 to 5."
                    );
            }
        }
    }
}