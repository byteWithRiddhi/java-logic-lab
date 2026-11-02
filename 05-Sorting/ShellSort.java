import java.util.Arrays;
import java.util.Scanner;

public class ShellSort {

    public static void shellSort(int[] arr) {
        int n = arr.length;

        // Start with a large gap and reduce it
        for (int gap = n / 2; gap > 0; gap /= 2) {

            // Perform insertion sort for each gap
            for (int i = gap; i < n; i++) {

                int temp = arr[i];
                int j = i;

                // Shift elements until the correct position is found
                while (j >= gap && arr[j - gap] > temp) {
                    arr[j] = arr[j - gap];
                    j -= gap;
                }

                arr[j] = temp;
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter array size: ");
        int n = sc.nextInt();

        int[] arr = new int[n];

        System.out.println("Enter array elements:");
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        shellSort(arr);

        System.out.println("Sorted array: " + Arrays.toString(arr));

        sc.close();
    }
}
