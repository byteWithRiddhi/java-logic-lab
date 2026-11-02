import java.util.Scanner;

public class FibonacciRecursion {

    static int fibonacci(int n) {

        // Base cases
        if (n == 0) {
            return 0;
        }

        if (n == 1) {
            return 1;
        }

        // Recursive calls
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of terms: ");
        int n = sc.nextInt();

        if (n < 0 || n > 46) {
            System.out.println("Enter a number between 0 and 46.");
        } else {
            System.out.println("Fibonacci Series:");

            for (int i = 0; i < n; i++) {
                System.out.print(fibonacci(i) + " ");
            }
        }

        sc.close();
    }
}
