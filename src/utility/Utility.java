package utility;

public class Utility {

    private Utility() {
        // Prevent object creation
    }

    public static void clearScreen() {
        System.out.println();
    }

    public static void printApplicationHeader() {

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("              SUBSCRIPTION & PAYMENT TRACKER");
        System.out.println("--------------------------------------------------------------");
        System.out.println("          Track subscriptions | Monitor renewals");
        System.out.println("                 Never miss a payment");
        System.out.println("==============================================================");
    }

    public static void printHeader(String title) {

        System.out.println();
        System.out.println("==============================================================");
        System.out.printf("                     %s%n", title);
        System.out.println("==============================================================");
    }

    public static void printSection(String title) {

        System.out.println();
        System.out.println("+------------------------------------------------------------+");
        System.out.printf("| %-58s |%n", title);
        System.out.println("+------------------------------------------------------------+");
    }

    public static void printSuccess(String message) {

        System.out.println();
        System.out.println("[SUCCESS] " + message);
    }

    public static void printError(String message) {

        System.out.println();
        System.out.println("[ERROR] " + message);
    }

    public static void printWarning(String message) {

        System.out.println();
        System.out.println("[WARNING] " + message);
    }

    public static void printInfo(String message) {

        System.out.println();
        System.out.println("[INFO] " + message);
    }

    public static void printMenuOption(int number, String text) {

        System.out.printf("   %-3d | %s%n", number, text);
    }

    public static void printSeparator() {

        System.out.println(
                "--------------------------------------------------------------"
        );
    }

    public static void printTableHeader(
            String column1,
            String column2,
            String column3) {

        System.out.println(
                "----------------------------------------------------------------"
        );

        System.out.printf(
                "%-8s %-25s %-25s%n",
                column1,
                column2,
                column3
        );

        System.out.println(
                "----------------------------------------------------------------"
        );
    }

    public static void printProfileBox(
            int userId,
            String name,
            String email,
            String phone,
            String status) {

        System.out.println();
        System.out.println("+------------------------------------------------------------+");
        System.out.println("|                     MY PROFILE                             |");
        System.out.println("+------------------------------------------------------------+");

        System.out.printf(
                "| User ID : %-48d |%n",
                userId
        );

        System.out.printf(
                "| Name    : %-48s |%n",
                name
        );

        System.out.printf(
                "| Email   : %-48s |%n",
                email
        );

        System.out.printf(
                "| Phone   : %-48s |%n",
                phone
        );

        System.out.printf(
                "| Status  : %-48s |%n",
                status
        );

        System.out.println("+------------------------------------------------------------+");
    }

    public static void printWelcomeUser(String name) {

        System.out.println();
        System.out.println("+------------------------------------------------------------+");
        System.out.printf(
                "| Welcome back, %-43s |%n",
                name
        );
        System.out.println("+------------------------------------------------------------+");
    }

    public static void printGoodbye() {

        clearScreen();

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("             THANK YOU FOR USING THE TRACKER");
        System.out.println("==============================================================");
        System.out.println("        Your subscriptions deserve attention too.");
        System.out.println("==============================================================");
        System.out.println();
    }

    public static void pause() {

        InputUtility.waitForEnter();
    }
}