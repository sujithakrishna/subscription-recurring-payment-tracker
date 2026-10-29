package notification;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationService {

    public static void createNotification(
            int userId,
            int subscriptionId,
            String type,
            String title,
            String message) {

        String sql = """
                INSERT INTO notifications
                (
                    user_id,
                    subscription_id,
                    notification_type,
                    title,
                    message,
                    is_read
                )
                VALUES (?, ?, ?, ?, ?, FALSE)
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    userId
            );

            statement.setInt(
                    2,
                    subscriptionId
            );

            statement.setString(
                    3,
                    type
            );

            statement.setString(
                    4,
                    title
            );

            statement.setString(
                    5,
                    message
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            Utility.printWarning(
                    "Notification could not be created."
            );
        }
    }

    public static void showNotifications(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "MY NOTIFICATIONS"
        );

        String sql = """
                SELECT
                    notification_id,
                    notification_type,
                    title,
                    message,
                    is_read,
                    created_at
                FROM notifications
                WHERE user_id = ?
                ORDER BY notification_id DESC
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    user.getUserId()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                boolean found = false;

                while (resultSet.next()) {

                    found = true;

                    System.out.println();

                    System.out.println(
                            "+------------------------------------------------------------+"
                    );

                    System.out.println(
                            "| "
                                    + resultSet.getString(
                                    "title"
                            )
                    );

                    System.out.println(
                            "+------------------------------------------------------------+"
                    );

                    System.out.println(
                            resultSet.getString(
                                    "message"
                            )
                    );

                    System.out.println();

                    System.out.println(
                            "Type : "
                                    + resultSet.getString(
                                    "notification_type"
                            )
                    );

                    System.out.println(
                            "Date : "
                                    + resultSet.getTimestamp(
                                    "created_at"
                            )
                    );

                    System.out.println(
                            "Read : "
                                    + (
                                    resultSet.getBoolean(
                                            "is_read"
                                    )
                                            ? "YES"
                                            : "NO"
                            )
                    );
                }

                if (!found) {

                    Utility.printInfo(
                            "No notifications available."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve notifications."
            );
        }

        markAsRead(user);

        Utility.pause();
    }

    private static void markAsRead(User user) {

        String sql = """
                UPDATE notifications
                SET is_read = TRUE
                WHERE user_id = ?
                AND is_read = FALSE
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    user.getUserId()
            );

            statement.executeUpdate();

        } catch (SQLException ignored) {
        }
    }
}