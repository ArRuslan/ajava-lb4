package ua.nure.jfm.task4.client;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private final Client client;
    private final Thread clientThread;

    public static void main(String[] args) throws IOException {
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
        client = new Client(address, port);
        clientThread = new Thread(() -> {
            try {
                client.connect();
            } catch (IOException e) {
                System.err.println("Error occurred while starting client: "+e);
            }
        });
    }

    private void run() {
        clientThread.start();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Login: ");
        String login = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        client.authenticate(login, password);

        while(client.isRunning()) {
            System.out.print("> ");
            String message = scanner.nextLine().trim();
            if(message.equals("exit")) {
                try {
                    client.disconnect();
                    break;
                } catch (IOException ignored) {
                }
            }

            client.sendMessage(message);
        }
    }
}