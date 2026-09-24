import java.util.Arrays;
import java.util.Scanner;

public class SortDescending {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter the number of elements: ");
        int n = sc.nextInt();

        Integer[] arr = new Integer[n];

        System.out.println("Enter array elements:");

        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }

        // Sort the array in ascending order first
        Arrays.sort(arr);

        System.out.println("Array in descending order:");

        // Print the array in reverse order
        for (int i = n - 1; i >= 0; i--) {
            System.out.print(arr[i] + " ");
        }

        sc.close();
    }
}
