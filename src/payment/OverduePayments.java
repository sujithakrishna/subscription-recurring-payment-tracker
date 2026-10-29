package payment;

import config.DBConnection;
import model.User;
import utility.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OverduePayments {

    public static void show(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "OVERDUE PAYMENTS"
        );

        PaymentScheduleService
                .synchronizeSchedules(user);

        String sql = """
                SELECT
                    ps.schedule_id,
                    ps.subscription_id,
                    p.provider_name,
                    p.plan_name,
                    ps.amount,
                    ps.scheduled_date
                FROM payment_schedules ps
                JOIN subscriptions s
                    ON ps.subscription_id =
                       s.subscription_id
                JOIN subscription_plans p
                    ON s.plan_id = p.plan_id
                WHERE s.user_id = ?
                AND ps.schedule_status = 'SCHEDULED'
                AND ps.scheduled_date < CURRENT_DATE
                ORDER BY ps.scheduled_date ASC
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

                System.out.println();

                System.out.println(
                        "--------------------------------------------------------------------------"
                );

                System.out.printf(
                        "%-6s %-7s %-16s %-14s %-14s%n",
                        "SCH ID",
                        "SUB ID",
                        "PROVIDER",
                        "PLAN",
                        "AMOUNT"
                );

                System.out.println(
                        "--------------------------------------------------------------------------"
                );

                while (resultSet.next()) {

                    found = true;

                    System.out.printf(
                            "%-6d %-7d %-16s %-14s ₹%-13.2f%n",
                            resultSet.getInt(
                                    "schedule_id"
                            ),
                            resultSet.getInt(
                                    "subscription_id"
                            ),
                            resultSet.getString(
                                    "provider_name"
                            ),
                            resultSet.getString(
                                    "plan_name"
                            ),
                            resultSet.getDouble(
                                    "amount"
                            )
                    );

                    System.out.println(
                            "       Due Date: "
                                    + resultSet.getDate(
                                    "scheduled_date"
                            )
                    );
                }

                System.out.println(
                        "--------------------------------------------------------------------------"
                );

                if (!found) {

                    Utility.printInfo(
                            "No overdue payments found."
                    );
                }
            }

        } catch (SQLException e) {

            Utility.printError(
                    "Unable to retrieve overdue payments."
            );

            System.out.println(
                    "Database message: "
                            + e.getMessage()
            );
        }

        Utility.pause();
    }
}