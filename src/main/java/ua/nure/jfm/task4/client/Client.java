package ua.nure.jfm.task4.client;

import ua.nure.jfm.task4.exceptions.EOFException;
import ua.nure.jfm.task4.packets.BasePacket;
import ua.nure.jfm.task4.packets.LoginPacket;
import ua.nure.jfm.task4.packets.NewMessagePacket;
import ua.nure.jfm.task4.packets.SendMessagePacket;

import java.io.*;
import java.net.Socket;

public class Client {
    private final String address;
    private final int port;
    private Socket socket;
    private boolean running = true;

    private BufferedReader reader;
    private BufferedWriter writer;

    public Client(String address, int port) {
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

        loop();
    }

    public void authenticate(String login, String password) {
        if(socket == null) {
            throw new IllegalStateException("Client is not connected to server!");
        }

        try {
            send(new LoginPacket(login, password));
        } catch (IOException e) {
            System.err.println("Failed to authenticate:" + e);
        }
    }

    private void loop() {
        while(!socket.isClosed()) {
            BasePacket packet;
            try {
                packet = BasePacket.readPacket(reader);
            } catch (IOException e) {
                System.err.println("Failed to read packet:" + e);
                continue;
            } catch (EOFException e) {
                System.err.println("Disconnected!");
                break;
            }

            if (packet instanceof NewMessagePacket messagePacket) {
                System.out.println("[" + messagePacket.login + "]  " + messagePacket.text);
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

    public void sendMessage(String text) {
        if(!isRunning()) {
            throw new IllegalStateException("Client is not connected to server!");
        }

        try {
            send(new SendMessagePacket(text));
        } catch (IOException e) {
            System.err.println("Failed to send message:" + e);
        }
    }
}
