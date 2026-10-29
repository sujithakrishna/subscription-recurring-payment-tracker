package subscription;

import config.DBConnection;
import model.User;
import utility.InputUtility;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CancelSubscription {

    public static void cancelSubscription(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "CANCEL SUBSCRIPTION"
        );

        int subscriptionId =
                InputUtility.readPositiveInt(
                        "Subscription ID: "
                );

        boolean confirm =
                InputUtility.readYesNo(
                        "Confirm cancellation? (Y/N): "
                );

        if (!confirm) {

            Utility.printInfo(
                    "Cancellation cancelled."
            );

            Utility.pause();

            return;
        }

        String sql = """
                UPDATE subscriptions
                SET status = 'CANCELLED',
                    auto_renew = FALSE,
                    end_date = CURRENT_DATE
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
            statement.setInt(2, user.getUserId());

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 1) {

                Utility.printSuccess(
                        "Subscription cancelled successfully."
                );

            } else {

                Utility.printError(
                        "Subscription not found or already cancelled."
                );
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to cancel subscription."
            );

            System.out.println(
                    "Database message: " + e.getMessage()
            );
        }

        Utility.pause();
    }
}