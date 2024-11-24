package ua.nure.jfm.task4.server;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private final Server server;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Server address [0.0.0.0]: ");
        String address = scanner.nextLine().trim();
        if(address.isEmpty()) {
            address = "0.0.0.0";
        }

        System.out.print("Server port: ");
        int port = scanner.nextInt();

        System.out.println("Starting server...");
        new Main(address, port).run();
    }

    Main(String address, int port) {
        server = new Server(address, port);
    }

    private void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nStopping server...");

            try {
                server.stop();
            } catch (IOException e) {
                System.err.println("Error occurred while stopping server: "+e);
            }
        }));

        try {
            server.run();
        } catch (IOException e) {
            System.err.println("Error occurred while starting server: "+e);
        }
    }
}