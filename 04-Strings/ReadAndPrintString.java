import java.util.Scanner;

public class ReadAndPrintString {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a string: ");
        String text = sc.nextLine();

        System.out.println("Entered string: " + text);

        sc.close();
    }
}
