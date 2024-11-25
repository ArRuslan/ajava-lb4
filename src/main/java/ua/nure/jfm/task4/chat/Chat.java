package ua.nure.jfm.task4.chat;

import ua.nure.jfm.task4.exceptions.EOFException;
import ua.nure.jfm.task4.packets.*;

import java.io.*;
import java.net.Socket;

public class Chat {
    private final String address;
    private final int port;
    private Socket socket;

    private BufferedReader reader;
    private BufferedWriter writer;

    public Chat(String address, int port) {
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
                System.out.println("[" + messagePacket.login + "] " + messagePacket.text);
            } else if (packet instanceof ServerErrorPacket errorPacket) {
                System.out.println("[Server] Error #" + errorPacket.code + ": " + errorPacket.message);
            }
        }
    }
}
