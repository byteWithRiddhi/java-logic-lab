import java.util.Scanner;

public class CreateAndDisplayLinkedList {

    // Node structure
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
    static void insert(int data) {
        Node newNode = new Node(data);

        if (head == null) {
            head = newNode;
            return;
        }

        Node temp = head;

        while (temp.next != null) {
            temp = temp.next;
        }

        temp.next = newNode;
    }

    // Display linked list
    static void display() {
        Node temp = head;

        System.out.println("Linked List:");

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
                System.out.print("Enter data for node " + i + ": ");
                int data = sc.nextInt();

                insert(data);
            }

            display();
        }

        sc.close();
    }
}
