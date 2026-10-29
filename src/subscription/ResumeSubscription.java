package subscription;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResumeSubscription {

    public static void resumeSubscription(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "RESUME SUBSCRIPTION"
        );

        int subscriptionId =
                InputUtility.readPositiveInt(
                        "Subscription ID: "
                );

        String sql = """
                UPDATE subscriptions
                SET status = 'ACTIVE'
                WHERE subscription_id = ?
                AND user_id = ?
                AND status = 'PAUSED'
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
                        "Subscription resumed successfully."
                );

            } else {

                Utility.printError(
                        "Only paused subscriptions can be resumed."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to resume subscription."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }
}