package utility;

import java.util.Scanner;

public class InputUtility {

    private static final Scanner scanner =
            new Scanner(System.in);

    private InputUtility() {
    }

    public static String readString(String message) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println(
                    "[WARNING] Input cannot be empty. Please try again."
            );
        }
    }

    public static int readInt(String message) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "[WARNING] Please enter a valid number."
                );
            }
        }
    }

    public static int readPositiveInt(String message) {

        while (true) {

            int value = readInt(message);

            if (value > 0) {
                return value;
            }

            System.out.println(
                    "[WARNING] Value must be greater than zero."
            );
        }
    }

    public static double readDouble(String message) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            try {

                return Double.parseDouble(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "[WARNING] Please enter a valid amount."
                );
            }
        }
    }

    public static double readPositiveDouble(String message) {

        while (true) {

            double value = readDouble(message);

            if (value > 0) {
                return value;
            }

            System.out.println(
                    "[WARNING] Amount must be greater than zero."
            );
        }
    }

    public static boolean readYesNo(String message) {

        while (true) {

            System.out.print(message);

            String input =
                    scanner.nextLine()
                           .trim()
                           .toLowerCase();

            if (input.equals("y")
                    || input.equals("yes")) {

                return true;
            }

            if (input.equals("n")
                    || input.equals("no")) {

                return false;
            }

            System.out.println(
                    "[WARNING] Please enter Y or N."
            );
        }
    }

    public static String readEmail(String message) {

        while (true) {

            String email = readString(message);

            if (email.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

                return email;
            }

            System.out.println(
                    "[WARNING] Please enter a valid email address."
            );
        }
    }

    public static String readPhone(String message) {

        while (true) {

            String phone = readString(message);

            if (phone.matches("\\d{10}")) {

                return phone;
            }

            System.out.println(
                    "[WARNING] Phone number must contain exactly 10 digits."
            );
        }
    }

    public static String readPassword(String message) {

        return readString(message);
    }

    public static void waitForEnter() {

        System.out.println();
        System.out.print("Press ENTER to continue...");

        scanner.nextLine();
    }
}