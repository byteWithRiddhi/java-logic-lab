import java.util.Scanner;

public class DuplicateCharacters {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a string: ");
        String text = sc.nextLine().toLowerCase();

        boolean found = false;

        System.out.println("Duplicate characters:");

        for (int i = 0; i < text.length(); i++) {

            char current = text.charAt(i);

            if (current == ' ') {
                continue;
            }

            boolean alreadyChecked = false;

            for (int k = 0; k < i; k++) {
                if (text.charAt(k) == current) {
                    alreadyChecked = true;
                    break;
                }
            }

            if (alreadyChecked) {
                continue;
            }

            int count = 0;

            for (int j = 0; j < text.length(); j++) {
                if (text.charAt(j) == current) {
                    count++;
                }
            }

            if (count > 1) {
                System.out.println(current + " appears " + count + " times");
                found = true;
            }
        }

        if (!found) {
            System.out.println("No duplicate characters found.");
        }

        sc.close();
    }
}
