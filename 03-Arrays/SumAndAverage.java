import java.util.Scanner;

public class SumAndAverage {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of elements: ");
        int n = sc.nextInt();

        int[] numbers = new int[n];
        int sum = 0;

        System.out.println("Enter array elements:");

        for (int i = 0; i < n; i++) {
            numbers[i] = sc.nextInt();
            sum += numbers[i];
        }

        double average = (double) sum / n;

        System.out.println("Sum of array elements: " + sum);
        System.out.println("Average of array elements: " + average);

        sc.close();
    }
}
