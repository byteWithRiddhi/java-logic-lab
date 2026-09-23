import java.util.Scanner;

public class ButterflyPattern {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of rows: ");
        int rows = sc.nextInt();

        // Upper half
        for (int i = 1; i <= rows; i++) {

            // Left stars
            for (int j = 1; j <= i; j++) {
                System.out.print("* ");
            }

            // Spaces
            for (int j = 1; j <= 2 * (rows - i); j++) {
                System.out.print("  ");
            }

            // Right stars
            for (int j = 1; j <= i; j++) {
                System.out.print("* ");
            }

            System.out.println();
        }

        // Lower half
        for (int i = rows; i >= 1; i--) {

            // Left stars
            for (int j = 1; j <= i; j++) {
                System.out.print("* ");
            }

            // Spaces
            for (int j = 1; j <= 2 * (rows - i); j++) {
                System.out.print("  ");
            }

            // Right stars
            for (int j = 1; j <= i; j++) {
                System.out.print("* ");
            }

            System.out.println();
        }

        sc.close();
    }
}
