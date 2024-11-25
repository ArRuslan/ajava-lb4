package ua.nure.jfm.task4.shutdown;

import ua.nure.jfm.task4.packets.ShutdownRequestPacket;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private final ShutdownClient client;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Server address [0.0.0.0]: ");
        String address = scanner.nextLine().trim();
        if(address.isEmpty()) {
            address = "0.0.0.0";
        }

        System.out.print("Server port: ");
        int port = scanner.nextInt();

        System.out.println("Connecting...");
        new Main(address, port).run();
    }

    Main(String address, int port) {
        client = new ShutdownClient(address, port, this::authenticateCallback, this::authenticatedCallback);
    }

    private void authenticateCallback() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Login: ");
        String login = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        client.authenticate(login, password);
    }

    private void authenticatedCallback() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Shutdown password: ");
        String password = scanner.nextLine().trim();

        try {
            client.send(new ShutdownRequestPacket(password));
        } catch (IOException e) {
            System.err.println("Error occurred while sending shutdown request: "+e);
        }
    }

    private void run() {
        try {
            client.connect();
        } catch (IOException e) {
            System.err.println("Error occurred while starting chat: "+e);
        }
    }
}