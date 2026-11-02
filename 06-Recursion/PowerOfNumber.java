import java.util.Scanner;

public class PowerOfNumber {

    static long power(int base, int exponent) {
        if (exponent == 0) {
            return 1;
        }

        return base * power(base, exponent - 1);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter base: ");
        int base = sc.nextInt();

        System.out.print("Enter exponent: ");
        int exponent = sc.nextInt();

        if (exponent < 0 || exponent > 62) {
            System.out.println("Enter an exponent between 0 and 62.");
        } else {
            System.out.println("Result: " + power(base, exponent));
        }

        sc.close();
    }
}
