import java.util.Scanner;

public class DecimalToBinary {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a decimal number: ");
        int n = sc.nextInt();

        int original = n;
        int binary = 0;
        int place = 1;

        if (n == 0) {
            binary = 0;
        } else {

            while (n > 0) {

                int remainder = n % 2;

                binary = binary + remainder * place;

                n = n / 2;
                place = place * 10;
            }
        }

        System.out.println("Binary equivalent of "
                + original + " is: " + binary);

        sc.close();
    }
}
