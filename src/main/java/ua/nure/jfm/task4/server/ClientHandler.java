package ua.nure.jfm.task4.server;

import ua.nure.jfm.task4.exceptions.EOFException;
import ua.nure.jfm.task4.packets.BasePacket;

import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private String login;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private Thread thread;

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        thread = new Thread(this::realHandle);
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return this.login;
    }

    private void realHandle() {
        while (!socket.isClosed()) {
            BasePacket packet;
            try {
                packet = BasePacket.readPacket(reader);
            } catch (IOException e) {
                System.err.println("Failed to read packet:" + e);
                continue;
            } catch (EOFException e) {
                System.err.println("Client disconnected!");
                break;
            }
            System.out.println("Got packet: " + packet + " of type " + packet.getPacketType());
        }

        server.clientDisconnected(this);
    }

    public BasePacket readPacket() throws IOException, EOFException {
        return BasePacket.readPacket(reader);
    }

    public void handle() {
        if(!thread.isAlive())
            thread.start();
    }

    synchronized public void close() {
        try {
            socket.close();
            thread.interrupt();
            thread.wait();
        } catch (IOException | InterruptedException ignored) {
        }
    }

    synchronized public void send(BasePacket packet) throws IOException {
        writer.write(packet.getPacketType().ordinal());
        for(byte byt : packet.encode()) { // WHY????
            writer.write(byt);
        }
        writer.flush();
    }
}
