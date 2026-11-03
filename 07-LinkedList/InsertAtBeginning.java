import java.util.Scanner;

public class InsertAtBeginning {

    static class Node {
        int data;
        Node next;

        Node(int data) {
            this.data = data;
            this.next = null;
        }
    }

    static Node head = null;

    // Insert node at beginning
    static void insertAtBeginning(int data) {
        Node newNode = new Node(data);

        newNode.next = head;
        head = newNode;
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

        for (int i = 1; i <= n; i++) {
            System.out.print("Enter data: ");
            int data = sc.nextInt();

            insertAtBeginning(data);
        }

        display();

        sc.close();
    }
}
