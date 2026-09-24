import java.util.Scanner;

public class ReverseArray {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of elements: ");
        int n = sc.nextInt();

        int[] numbers = new int[n];

        System.out.println("Enter array elements:");

        for (int i = 0; i < n; i++) {
            numbers[i] = sc.nextInt();
        }

        System.out.println("Array in reverse order:");

        for (int i = n - 1; i >= 0; i--) {
            System.out.print(numbers[i] + " ");
        }

        sc.close();
    }
}
