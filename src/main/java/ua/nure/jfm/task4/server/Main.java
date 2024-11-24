package ua.nure.jfm.task4.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Starting server...");
        new Server("0.0.0.0", 11111).run();
    }
}