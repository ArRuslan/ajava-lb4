package ua.nure.jfm.task4.server;

import ua.nure.jfm.task4.packets.BasePacket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
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

    private void realHandle() {
        while(!socket.isClosed()) {

        }
    }

    public BasePacket readPacket() throws IOException {
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

    synchronized public void send(BasePacket packet) {
        //
    }
}
