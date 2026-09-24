import java.util.Scanner;

public class RotateArray {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of elements: ");
        int n = sc.nextInt();

        int[] numbers = new int[n];

        System.out.println("Enter array elements:");

        for (int i = 0; i < n; i++) {
            numbers[i] = sc.nextInt();
        }

        int last = numbers[n - 1];

        // Shift elements one position to the right
        for (int i = n - 1; i > 0; i--) {
            numbers[i] = numbers[i - 1];
        }

        numbers[0] = last;

        System.out.println("Array after right rotation:");

        for (int i = 0; i < n; i++) {
            System.out.print(numbers[i] + " ");
        }

        sc.close();
    }
}
