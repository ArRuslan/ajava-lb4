package ua.nure.jfm.task4.client;

import ua.nure.jfm.task4.packets.BasePacket;
import ua.nure.jfm.task4.packets.LoginPacket;

import java.io.*;
import java.net.Socket;

public class Client {
    private final String address;
    private final int port;
    private Socket socket;

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

    synchronized public void connect() throws IOException {
        if(socket != null) {
            throw new IllegalStateException("Client is already connected to server!");
        }

        socket = new Socket(address, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        send(new LoginPacket("test", "test"));
        BasePacket packet = BasePacket.readPacket(reader);
        System.out.println("Got packet: " + packet + " of type " + packet.getPacketType());
    }
}
