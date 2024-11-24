package ua.nure.jfm.task4.client;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Starting client...");
        new Client("0.0.0.0", 11111).connect();
    }
}