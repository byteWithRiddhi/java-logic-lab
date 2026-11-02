import java.util.Scanner;

public class SumOfNaturalNumbers {

    static int calculateSum(int n) {
        if (n == 0) {
            return 0;
        }

        return n + calculateSum(n - 1);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter N: ");
        int n = sc.nextInt();

        System.out.println("Sum: " + calculateSum(n));

        sc.close();
    }
}
