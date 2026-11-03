import java.util.Scanner;

public class InsertAtEnd {

    static class Node {
        int data;
        Node next;

        Node(int data) {
            this.data = data;
            this.next = null;
        }
    }

    static Node head = null;

    // Insert node at the end
    static void insertAtEnd(int data) {
        Node newNode = new Node(data);

        // If list is empty
        if (head == null) {
            head = newNode;
            return;
        }

        // Traverse to the last node
        Node temp = head;

        while (temp.next != null) {
            temp = temp.next;
        }

        // Link new node at the end
        temp.next = newNode;
    }

    // Display linked list
    static void display() {
        Node temp = head;

        System.out.print("Linked List: ");

        while (temp != null) {
            System.out.print(temp.data + " -> ");
            temp = temp.next;
        }

        System.out.println("null");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of nodes: ");
        int n = sc.nextInt();

        if (n < 0) {
            System.out.println("Number of nodes cannot be negative.");
        } else {
            for (int i = 1; i <= n; i++) {
                System.out.print("Enter data: ");
                int data = sc.nextInt();

                insertAtEnd(data);
            }

            display();
        }

        sc.close();
    }
}
