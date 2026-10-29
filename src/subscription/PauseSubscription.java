package subscription;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PauseSubscription {

    public static void pauseSubscription(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "PAUSE SUBSCRIPTION"
        );

        int subscriptionId =
                InputUtility.readPositiveInt(
                        "Subscription ID: "
                );

        boolean confirm =
                InputUtility.readYesNo(
                        "Pause this subscription? (Y/N): "
                );

        if (!confirm) {

            Utility.printInfo(
                    "Pause operation cancelled."
            );

            Utility.pause();

            return;
        }

        String sql = """
                UPDATE subscriptions
                SET status = 'PAUSED'
                WHERE subscription_id = ?
                AND user_id = ?
                AND status = 'ACTIVE'
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, subscriptionId);
            statement.setInt(2, user.getUserId());

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 1) {

                Utility.printSuccess(
                        "Subscription paused successfully."
                );

            } else {

                Utility.printError(
                        "Only active subscriptions can be paused."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to pause subscription."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }
}