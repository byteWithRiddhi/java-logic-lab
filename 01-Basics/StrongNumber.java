import java.util.Scanner;

public class StrongNumber {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a number: ");
        int n = sc.nextInt();

        int original = n;
        int sum = 0;

        while (n != 0) {

            int digit = n % 10;

            // Calculate factorial of digit
            int factorial = 1;

            for (int i = 1; i <= digit; i++) {
                factorial = factorial * i;
            }

            sum = sum + factorial;
            n = n / 10;
        }

        if (sum == original) {
            System.out.println(original + " is a strong number.");
        } else {
            System.out.println(original + " is not a strong number.");
        }

        sc.close();
    }
}
