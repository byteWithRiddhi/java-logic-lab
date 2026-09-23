import java.util.Scanner;

public class PascalsTriangle {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of rows: ");
        int rows = sc.nextInt();

        for (int i = 0; i < rows; i++) {

            // Print spaces
            for (int j = i; j < rows - 1; j++) {
                System.out.print(" ");
            }

            int number = 1;

            // Print numbers
            for (int j = 0; j <= i; j++) {
                System.out.print(number + " ");

                number = number * (i - j) / (j + 1);
            }

            System.out.println();
        }

        sc.close();
    }
}
