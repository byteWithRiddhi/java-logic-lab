import java.util.Scanner;

public class CharacterFrequency {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a string: ");
        String text = sc.nextLine().toLowerCase();

        boolean[] visited = new boolean[text.length()];

        System.out.println("Character frequencies:");

        for (int i = 0; i < text.length(); i++) {

            if (text.charAt(i) == ' ' || visited[i]) {
                continue;
            }

            char current = text.charAt(i);
            int count = 0;

            for (int j = 0; j < text.length(); j++) {

                if (text.charAt(j) == current) {
                    count++;
                    visited[j] = true;
                }
            }

            System.out.println(current + " : " + count);
        }

        sc.close();
    }
}
