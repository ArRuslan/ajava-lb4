package ua.nure.jfm.task4.chat;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private final Chat client;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Server address [0.0.0.0]: ");
        String address = scanner.nextLine().trim();
        if (address.isEmpty()) {
            address = "0.0.0.0";
        }

        System.out.print("Server port: ");
        int port = scanner.nextInt();

        System.out.println("Connecting...");
        new Main(address, port).run();
    }

    Main(String address, int port) {
        client = new Chat(address, port, this::authenticateCallback);
    }

    private void authenticateCallback() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Login: ");
        String login = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        client.authenticate(login, password);
    }

    private void run() {
        try {
            client.connect();
        } catch (IOException e) {
            System.err.println("Error occurred while starting chat: " + e);
        }
    }
}