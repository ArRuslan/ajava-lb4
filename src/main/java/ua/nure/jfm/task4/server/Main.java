package ua.nure.jfm.task4.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private final Server server;

    public static void main(String[] args) {
        Properties props = new Properties();
        InputStream stream = Main.class.getClassLoader().getResourceAsStream("server.properties");
        if (stream == null) {
            System.err.println("Failed to load server settings: stream is null");
        } else {
            try {
                props.load(stream);
                stream.close();
            } catch (IOException e) {
                System.err.println("Failed to load server settings: " + e);
            }
        }

        String address;
        int port = 0;
        String shutdownPassword;

        Scanner scanner = new Scanner(System.in);
        if(props.containsKey("address")) {
            address = (String)props.get("address");
        } else {
            System.out.print("Server address [0.0.0.0]: ");
            address = scanner.nextLine().trim();
            if (address.isEmpty()) {
                address = "0.0.0.0";
            }
        }

        if(props.containsKey("port")) {
            try {
                port = Integer.parseInt((String) props.get("port"));
            } catch (NumberFormatException e) {
                System.err.print("Invalid port specified in server settings!");
            }
        }
        if (port == 0) {
            System.out.print("Server port: ");
            port = scanner.nextInt();
        }

        if(props.containsKey("shutdown_password")) {
            shutdownPassword = (String)props.get("shutdown_password");
        } else {
            System.out.print("Shutdown password: ");
            shutdownPassword = scanner.nextLine().trim();
        }

        System.out.println("Starting server...");
        new Main(address, port, shutdownPassword).run();
    }

    Main(String address, int port, String shutdownPassword) {
        server = new Server(address, port);
        server.setShutdownPassword(shutdownPassword);
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