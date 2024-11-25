package ua.nure.jfm.task4.chat;

import ua.nure.jfm.task4.common.ClientBase;

public class Chat extends ClientBase {
    private final Runnable authenticationCallback;

    public Chat(String address, int port, Runnable authenticationCallback) {
        super(address, port);
        this.authenticationCallback = authenticationCallback;
    }

    protected void onAuthentication() {
        authenticationCallback.run();
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
    }
}
