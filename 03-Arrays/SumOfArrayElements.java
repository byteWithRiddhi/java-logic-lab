import java.util.Scanner;

public class SumOfArrayElements {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of elements: ");
        int n = sc.nextInt();

        int[] numbers = new int[n];
        int sum = 0;

        System.out.println("Enter array elements:");

        for (int i = 0; i < n; i++) {
            numbers[i] = sc.nextInt();
            sum = sum + numbers[i];
        }

        System.out.println("Sum of array elements: " + sum);

        sc.close();
    }
}
