package subscription;

import config.DBConnection;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddSubscriptionPlan {

    public static int addNewPlan() {

        Utility.clearScreen();

        Utility.printHeader("ADD NEW SUBSCRIPTION");

        System.out.println();
        System.out.println(
                "Add a subscription that is not available in the list."
        );
        System.out.println();

        String providerName =
                InputUtility.readString(
                        "Provider Name  : "
                );

        String planName =
                InputUtility.readString(
                        "Plan Name      : "
                );

        String category =
                InputUtility.readString(
                        "Category       : "
                );

        String description =
                InputUtility.readString(
                        "Description    : "
                );

        if (planExists(providerName, planName)) {

            Utility.printWarning(
                    "This subscription plan already exists."
            );

            return -1;
        }

        String sql = """
                INSERT INTO subscription_plans
                (
                    plan_name,
                    provider_name,
                    category,
                    description,
                    status
                )
                VALUES (?, ?, ?, ?, 'ACTIVE')
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
                    planName
            );

            statement.setString(
                    2,
                    providerName
            );

            statement.setString(
                    3,
                    category
            );

            statement.setString(
                    4,
                    description
            );

            int rowsInserted =
                    statement.executeUpdate();

            if (rowsInserted == 1) {

                try (
                        ResultSet generatedKeys =
                                statement.getGeneratedKeys()
                ) {

                    if (generatedKeys.next()) {

                        int planId =
                                generatedKeys.getInt(1);

                        Utility.printSuccess(
                                "New subscription added to the catalog."
                        );

                        System.out.println();
                        System.out.println(
                                "Plan ID       : " + planId
                        );
                        System.out.println(
                                "Provider      : " + providerName
                        );
                        System.out.println(
                                "Plan          : " + planName
                        );
                        System.out.println(
                                "Category      : " + category
                        );

                        return planId;
                    }
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to add the new subscription."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        return -1;
    }

    private static boolean planExists(
            String providerName,
            String planName) {

        String sql = """
                SELECT plan_id
                FROM subscription_plans
                WHERE provider_name = ?
                AND plan_name = ?
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
                    providerName
            );

            statement.setString(
                    2,
                    planName
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to check existing subscription."
            );

            return true;
        }
    }
}