package ua.nure.jfm.task4.client;

import ua.nure.jfm.task4.common.ClientBase;
import ua.nure.jfm.task4.packets.SendMessagePacket;

import java.io.IOException;

public class Client extends ClientBase {
    private final Runnable authenticationCallback;
    private final Thread inputThread;

    public Client(String address, int port, Runnable authenticationCallback, Runnable inputLoop) {
        super(address, port);
        this.authenticationCallback = authenticationCallback;
        this.inputThread = new Thread(inputLoop);
    }

    public void sendMessage(String text) {
        if (!isRunning()) {
            throw new IllegalStateException("Client is not connected to server!");
        }

        try {
            send(new SendMessagePacket(text));
        } catch (IOException e) {
            onClientError("Failed to send message:" + e);
        }
    }

    protected void onAuthentication() {
        authenticationCallback.run();
    }

    protected void onAuthenticated() {
        inputThread.start();
    }

    protected void onServerError(int code, String message) {
        System.out.println("[Server] Error #" + code + ": " + message);
    }

    protected void onClientError(String message) {
        System.err.println(message);
    }

    protected void onNewMessage(String from, String text) {
        System.out.println("[" + from + "] " + text);
    }

    protected void onClientConnected(String login) {
        System.out.println("[Server] " + login + " connected!");
    }

    protected void onClientDisconnected(String login) {
        System.out.println("[Server] " + login + " disconnected!");
    }

    protected void onServerStopping() {
        System.err.println("Server is stopping...");
    }

    protected void onDisconnected() {
        System.err.println("Disconnected!");
        inputThread.interrupt();
    }
}
