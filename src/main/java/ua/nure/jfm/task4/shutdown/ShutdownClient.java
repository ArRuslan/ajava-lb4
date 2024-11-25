package ua.nure.jfm.task4.shutdown;

import ua.nure.jfm.task4.common.ClientBase;

public class ShutdownClient extends ClientBase {
    private final Runnable authenticationCallback;
    private final Runnable authenticatedCallback;

    public ShutdownClient(String address, int port, Runnable authenticationCallback, Runnable authenticatedCallback) {
        super(address, port);
        this.authenticationCallback = authenticationCallback;
        this.authenticatedCallback = authenticatedCallback;
    }

    protected void onAuthentication() {
        authenticationCallback.run();
    }

    protected void onAuthenticated() {
        authenticatedCallback.run();
    }

    protected void onServerError(int code, String message) {
        System.out.println("[Server] Error #" + code + ": " + message);
    }

    protected void onClientError(String message) {
        System.err.println(message);
    }

    protected void onDisconnected() {
        System.err.println("Disconnected!");
    }
}
