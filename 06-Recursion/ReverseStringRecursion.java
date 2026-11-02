import java.util.Scanner;

public class ReverseStringRecursion {

    static String reverse(String str) {

        // Base case
        if (str.isEmpty()) {
            return str;
        }

        // Recursive call
        return reverse(str.substring(1)) + str.charAt(0);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a string: ");
        String str = sc.nextLine();

        String reversed = reverse(str);

        System.out.println("Reversed String: " + reversed);

        sc.close();
    }
}
