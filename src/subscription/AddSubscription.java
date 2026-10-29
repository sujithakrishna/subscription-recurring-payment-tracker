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

public class AddSubscription {

    public static void addSubscription(User user) {

        while (true) {

            Utility.clearScreen();

            Utility.printHeader("ADD NEW SUBSCRIPTION");

            System.out.println();

            System.out.println("  1 | Choose from available subscriptions");
            System.out.println("  2 | Add a new subscription");
            System.out.println("  3 | Go back");

            System.out.println();

            Utility.printSeparator();

            int choice =
                    InputUtility.readInt(
                            "Select an option: "
                    );

            switch (choice) {

                case 1:
                    addFromExistingPlans(user);
                    return;

                case 2:
                    addNewSubscription(user);
                    return;

                case 3:
                    return;

                default:
                    Utility.printWarning(
                            "Please choose 1, 2 or 3."
                    );
                    Utility.pause();
            }
        }
    }

    private static void addFromExistingPlans(User user) {

        Utility.clearScreen();

        Utility.printHeader("AVAILABLE SUBSCRIPTIONS");

        displayAvailablePlans();

        System.out.println();

        int planId =
                InputUtility.readPositiveInt(
                        "Enter Plan ID: "
                );

        if (!planExists(planId)) {

            Utility.printError(
                    "Invalid or inactive subscription plan."
            );

            Utility.pause();

            return;
        }

        createUserSubscription(user, planId);
    }

    private static void addNewSubscription(User user) {

        int planId =
                AddSubscriptionPlan.addNewPlan();

        if (planId == -1) {

            Utility.pause();

            return;
        }

        System.out.println();

        createUserSubscription(user, planId);
    }

    private static void createUserSubscription(
            User user,
            int planId) {

        System.out.println();

        double amount =
                InputUtility.readPositiveDouble(
                        "Subscription Amount : ₹"
                );

        String billingCycle =
                readBillingCycle();

        LocalDate startDate =
                LocalDate.now();

        LocalDate nextBillingDate =
                DateUtility.calculateNextBillingDate(
                        startDate,
                        billingCycle
                );

        boolean autoRenew =
                InputUtility.readYesNo(
                        "Enable auto-renewal? (Y/N): "
                );

        String sql = """
                INSERT INTO subscriptions
                (
                    user_id,
                    plan_id,
                    amount,
                    billing_cycle,
                    start_date,
                    next_billing_date,
                    status,
                    auto_renew
                )
                VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', ?)
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

            statement.setInt(
                    1,
                    user.getUserId()
            );

            statement.setInt(
                    2,
                    planId
            );

            statement.setDouble(
                    3,
                    amount
            );

            statement.setString(
                    4,
                    billingCycle
            );

            statement.setDate(
                    5,
                    java.sql.Date.valueOf(startDate)
            );

            statement.setDate(
                    6,
                    java.sql.Date.valueOf(nextBillingDate)
            );

            statement.setBoolean(
                    7,
                    autoRenew
            );

            int rowsInserted =
                    statement.executeUpdate();

            if (rowsInserted == 1) {

                try (
                        ResultSet generatedKeys =
                                statement.getGeneratedKeys()
                ) {

                    if (generatedKeys.next()) {

                        int subscriptionId =
                                generatedKeys.getInt(1);

                        displaySuccess(
                                subscriptionId,
                                amount,
                                billingCycle,
                                startDate,
                                nextBillingDate,
                                autoRenew
                        );
                    }
                }

            } else {

                Utility.printError(
                        "Unable to add subscription."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Database error while adding subscription."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }

    private static void displayAvailablePlans() {

        String sql = """
                SELECT
                    plan_id,
                    plan_name,
                    provider_name,
                    category
                FROM subscription_plans
                WHERE status = 'ACTIVE'
                ORDER BY plan_id ASC
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            System.out.println();

            System.out.println(
                    "------------------------------------------------------------------------"
            );

            System.out.printf(
                    "%-6s %-20s %-20s %-20s%n",
                    "ID",
                    "PROVIDER",
                    "PLAN",
                    "CATEGORY"
            );

            System.out.println(
                    "------------------------------------------------------------------------"
            );

            boolean found = false;

            while (resultSet.next()) {

                found = true;

                System.out.printf(
                        "%-6d %-20s %-20s %-20s%n",
                        resultSet.getInt("plan_id"),
                        resultSet.getString("provider_name"),
                        resultSet.getString("plan_name"),
                        resultSet.getString("category")
                );
            }

            System.out.println(
                    "------------------------------------------------------------------------"
            );

            if (!found) {

                Utility.printInfo(
                        "No subscription plans are currently available."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to load subscription plans."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }
    }

    private static boolean planExists(int planId) {

        String sql = """
                SELECT plan_id
                FROM subscription_plans
                WHERE plan_id = ?
                AND status = 'ACTIVE'
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, planId);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to verify subscription plan."
            );

            return false;
        }
    }

    private static String readBillingCycle() {

        while (true) {

            System.out.println();

            System.out.println(
                    "+--------------------------------------+"
            );
            System.out.println(
                    "|          BILLING CYCLE               |"
            );
            System.out.println(
                    "+--------------------------------------+"
            );
            System.out.println(
                    "| 1 | Weekly                           |"
            );
            System.out.println(
                    "| 2 | Monthly                          |"
            );
            System.out.println(
                    "| 3 | Quarterly                        |"
            );
            System.out.println(
                    "| 4 | Half-Yearly                      |"
            );
            System.out.println(
                    "| 5 | Yearly                            |"
            );
            System.out.println(
                    "+--------------------------------------+"
            );

            int choice =
                    InputUtility.readInt(
                            "Choose cycle: "
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
                            "Please select a value from 1 to 5."
                    );
            }
        }
    }

    private static void displaySuccess(
            int subscriptionId,
            double amount,
            String billingCycle,
            LocalDate startDate,
            LocalDate nextBillingDate,
            boolean autoRenew) {

        System.out.println();

        System.out.println(
                "+------------------------------------------------------------+"
        );

        System.out.println(
                "|             SUBSCRIPTION ADDED SUCCESSFULLY               |"
        );

        System.out.println(
                "+------------------------------------------------------------+"
        );

        System.out.printf(
                "| Subscription ID : %-38d |%n",
                subscriptionId
        );

        System.out.printf(
                "| Amount          : ₹%-36.2f |%n",
                amount
        );

        System.out.printf(
                "| Billing Cycle   : %-38s |%n",
                billingCycle
        );

        System.out.printf(
                "| Start Date      : %-38s |%n",
                startDate
        );

        System.out.printf(
                "| Next Payment    : %-38s |%n",
                nextBillingDate
        );

        System.out.printf(
                "| Auto Renewal    : %-38s |%n",
                autoRenew ? "ENABLED" : "DISABLED"
        );

        System.out.println(
                "+------------------------------------------------------------+"
        );

        Utility.printSuccess(
                "Your subscription is now being tracked."
        );
    }
}