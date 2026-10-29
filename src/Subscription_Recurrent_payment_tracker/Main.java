package Subscription_Recurrent_payment_tracker;

import config.DBConnection;
import model.User;

import dashboard.Dashboard;

import notification.NotificationService;

import payment.AddPaymentMethod;
import payment.DeletePaymentMethod;
import payment.OverduePayments;
import payment.PaymentDetails;
import payment.PaymentHistory;
import payment.ProcessPayment;
import payment.RetryFailedPayment;
import payment.SetDefaultPaymentMethod;
import payment.UpdatePaymentMethod;
import payment.UpcomingPayments;
import payment.ViewPaymentMethods;

import report.ExpenseReport;

import reminder.PaymentReminder;

import subscription.AddSubscription;
import subscription.CancelSubscription;
import subscription.PauseSubscription;
import subscription.ResumeSubscription;
import subscription.SubscriptionDetails;
import subscription.UpdateSubscription;
import subscription.ViewActiveSubscriptions;
import subscription.ViewCancelledSubscriptions;
import subscription.ViewExpiredSubscriptions;
import subscription.ViewSubscriptions;

import user.DeleteUser;
import user.DisplayUsers;
import user.UpdateUser;
import user.UserLogin;
import user.UserRegistration;

import utility.InputUtility;
import utility.Utility;

public class Main {

    public static void main(String[] args) {

        showWelcomeScreen();

        DBConnection.testConnection();

        Utility.pause();

        mainMenu();
    }

    private static void showWelcomeScreen() {

        Utility.clearScreen();

        Utility.printApplicationHeader();

        System.out.println();

        System.out.println(
                "Welcome to your personal subscription control center."
        );

        System.out.println(
                "Manage recurring expenses, monitor payments,"
        );

        System.out.println(
                "and keep track of every renewal in one place."
        );

        System.out.println();
    }

    private static void mainMenu() {

        while (true) {

            Utility.clearScreen();

            Utility.printHeader(
                    "GET STARTED"
            );

            System.out.println();

            System.out.println(
                    "  1 | Create New Account"
            );

            System.out.println(
                    "  2 | User Login"
            );

            System.out.println(
                    "  3 | View Registered Users"
            );

            System.out.println(
                    "  4 | Exit Application"
            );

            System.out.println();

            Utility.printSeparator();

            int choice =
                    InputUtility.readInt(
                            "Select an option: "
                    );

            switch (choice) {

                case 1:

                    UserRegistration.registerUser();

                    break;

                case 2:

                    User loggedInUser =
                            UserLogin.login();

                    if (loggedInUser != null) {

                        userDashboard(
                                loggedInUser
                        );
                    }

                    break;

                case 3:

                    DisplayUsers.displayUsers();

                    break;

                case 4:

                    Utility.printGoodbye();

                    return;

                default:

                    Utility.printError(
                            "Invalid option. Please choose 1 - 4."
                    );

                    Utility.pause();
            }
        }
    }

    private static void userDashboard(User user) {

        while (true) {

            Utility.clearScreen();

            Utility.printHeader(
                    "USER DASHBOARD"
            );

            Utility.printWelcomeUser(
                    user.getFullName()
            );

            System.out.println();

            System.out.println("  ACCOUNT");

            System.out.println(
                    "  1  | My Profile"
            );

            System.out.println(
                    "  2  | Update Profile"
            );

            System.out.println(
                    "  3  | Deactivate Account"
            );

            System.out.println();

            System.out.println("  SUBSCRIPTIONS");

            System.out.println(
                    "  4  | My Subscriptions"
            );

            System.out.println(
                    "  5  | Add Subscription"
            );

            System.out.println(
                    "  6  | Subscription Details"
            );

            System.out.println(
                    "  7  | Update Subscription"
            );

            System.out.println(
                    "  8  | Pause Subscription"
            );

            System.out.println(
                    "  9  | Resume Subscription"
            );

            System.out.println(
                    "  10 | Cancel Subscription"
            );

            System.out.println();

            System.out.println(
                    "  SUBSCRIPTION VIEWS"
            );

            System.out.println(
                    "  11 | Active Subscriptions"
            );

            System.out.println(
                    "  12 | Expired Subscriptions"
            );

            System.out.println(
                    "  13 | Cancelled Subscriptions"
            );

            System.out.println();

            System.out.println(
                    "  PAYMENT METHODS"
            );

            System.out.println(
                    "  14 | Add Payment Method"
            );

            System.out.println(
                    "  15 | View Payment Methods"
            );

            System.out.println(
                    "  16 | Update Payment Method"
            );

            System.out.println(
                    "  17 | Set Default Payment Method"
            );

            System.out.println(
                    "  18 | Remove Payment Method"
            );

            System.out.println();

            System.out.println(
                    "  PAYMENTS"
            );

            System.out.println(
                    "  19 | Upcoming Payments"
            );

            System.out.println(
                    "  20 | Overdue Payments"
            );

            System.out.println(
                    "  21 | Process Payment"
            );

            System.out.println(
                    "  22 | Payment History"
            );

            System.out.println(
                    "  23 | Payment Details"
            );

            System.out.println(
                    "  24 | Retry Failed Payment"
            );

            System.out.println();

            System.out.println(
                    "  TRACKING"
            );

            System.out.println(
                    "  25 | Payment Reminders"
            );

            System.out.println(
                    "  26 | Notifications"
            );

            System.out.println(
                    "  27 | Expense Report"
            );

            System.out.println();

            System.out.println(
                    "  ACCOUNT OVERVIEW"
            );

            System.out.println(
                    "  28 | Account Overview"
            );

            System.out.println();

            System.out.println(
                    "  29 | Logout"
            );

            System.out.println();

            Utility.printSeparator();

            int choice =
                    InputUtility.readInt(
                            "Select an option: "
                    );

            switch (choice) {

                case 1:

                    showProfile(user);

                    break;

                case 2:

                    UpdateUser.updateUser(user);

                    break;

                case 3:

                    boolean deactivated =
                            DeleteUser.deleteUser(user);

                    if (deactivated) {
                        return;
                    }

                    break;

                case 4:

                    ViewSubscriptions
                            .viewSubscriptions(user);

                    break;

                case 5:

                    AddSubscription
                            .addSubscription(user);

                    break;

                case 6:

                    SubscriptionDetails
                            .showDetails(user);

                    break;

                case 7:

                    UpdateSubscription
                            .updateSubscription(user);

                    break;

                case 8:

                    PauseSubscription
                            .pauseSubscription(user);

                    break;

                case 9:

                    ResumeSubscription
                            .resumeSubscription(user);

                    break;

                case 10:

                    CancelSubscription
                            .cancelSubscription(user);

                    break;

                case 11:

                    ViewActiveSubscriptions
                            .viewActiveSubscriptions(user);

                    break;

                case 12:

                    ViewExpiredSubscriptions
                            .viewExpiredSubscriptions(user);

                    break;

                case 13:

                    ViewCancelledSubscriptions
                            .viewCancelledSubscriptions(user);

                    break;

                case 14:

                    AddPaymentMethod
                            .addPaymentMethod(user);

                    break;

                case 15:

                    ViewPaymentMethods
                            .viewPaymentMethods(user);

                    break;

                case 16:

                    UpdatePaymentMethod
                            .update(user);

                    break;

                case 17:

                    SetDefaultPaymentMethod
                            .setDefault(user);

                    break;

                case 18:

                    DeletePaymentMethod
                            .delete(user);

                    break;

                case 19:

                    UpcomingPayments
                            .show(user);

                    break;

                case 20:

                    OverduePayments
                            .show(user);

                    break;

                case 21:

                    ProcessPayment
                            .process(user);

                    break;

                case 22:

                    PaymentHistory
                            .show(user);

                    break;

                case 23:

                    PaymentDetails
                            .show(user);

                    break;

                case 24:

                    RetryFailedPayment
                            .retry(user);

                    break;

                case 25:

                    PaymentReminder
                            .generateReminders(user);

                    break;

                case 26:

                    NotificationService
                            .showNotifications(user);

                    break;

                case 27:

                    ExpenseReport
                            .show(user);

                    break;

                case 28:

                    Dashboard
                            .show(user);

                    break;

                case 29:

                    Utility.printSuccess(
                            "You have been logged out."
                    );

                    Utility.pause();

                    return;

                default:

                    Utility.printError(
                            "Invalid option. Please choose 1 - 29."
                    );

                    Utility.pause();
            }
        }
    }

    private static void showProfile(User user) {

        Utility.clearScreen();

        Utility.printHeader(
                "MY PROFILE"
        );

        Utility.printProfileBox(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus()
        );

        Utility.pause();
    }
}