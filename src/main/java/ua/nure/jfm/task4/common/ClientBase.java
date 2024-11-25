package ua.nure.jfm.task4.common;

import ua.nure.jfm.task4.exceptions.EOFException;
import ua.nure.jfm.task4.packets.*;

import java.io.*;
import java.net.Socket;

public class ClientBase {
    private final String address;
    private final int port;
    private Socket socket;
    private boolean running = true;

    private BufferedReader reader;
    private BufferedWriter writer;

    public ClientBase(String address, int port) {
        this.address = address;
        this.port = port;
    }

    synchronized public void send(BasePacket packet) throws IOException {
        writer.write(packet.getPacketType().ordinal());
        for(byte byt : packet.encode()) {
            writer.write(byt);
        }
        writer.flush();
    }

    public void connect() throws IOException {
        if(socket != null) {
            throw new IllegalStateException("Client is already connected to server!");
        }

        socket = new Socket(address, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        onAuthentication();
        loop();
    }

    public void authenticate(String login, String password) {
        if(socket == null) {
            throw new IllegalStateException("Client is not connected to server!");
        }

        try {
            send(new LoginPacket(login, password));
        } catch (IOException e) {
            onClientError("Failed to authenticate: " + e);
        }
    }

    private void loop() {
        while(!socket.isClosed()) {
            BasePacket packet;
            try {
                packet = BasePacket.readPacket(reader);
            } catch (IOException e) {
                onClientError("Failed to read packet: " + e);
                continue;
            } catch (EOFException e) {
                onDisconnected();
                break;
            }

            if (packet instanceof ServerHelloPacket) {
                onAuthenticated();
            } else if (packet instanceof NewMessagePacket messagePacket) {
                onNewMessage(messagePacket.login, messagePacket.text);
            } else if (packet instanceof ServerErrorPacket errorPacket) {
                onServerError(errorPacket.code, errorPacket.message);
            } else if (packet instanceof ClientConnectedPacket clientConnectedPacket) {
                onClientConnected(clientConnectedPacket.login);
            } else if (packet instanceof ClientDisconnectedPacket clientDisconnectedPacket) {
                onClientDisconnected(clientDisconnectedPacket.login);
            }
        }

        running = false;
    }

    public void disconnect() throws IOException {
        if(socket == null) {
            throw new IllegalStateException("Client is not connected to server!");
        }

        socket.close();
    }

    public boolean isRunning() {
        return socket != null && running;
    }

    protected void onAuthentication() {

    }

    protected void onAuthenticated() {

    }

    protected void onServerError(int code, String message) {

    }

    protected void onClientError(String message) {

    }

    protected void onNewMessage(String from, String text) {

    }

    protected void onClientConnected(String login) {

    }

    protected void onClientDisconnected(String login) {

    }

    protected void onDisconnected() {

    }
}
