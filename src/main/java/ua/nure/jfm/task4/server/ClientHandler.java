package ua.nure.jfm.task4.server;

import ua.nure.jfm.task4.exceptions.EOFException;
import ua.nure.jfm.task4.packets.BasePacket;
import ua.nure.jfm.task4.packets.SendMessagePacket;
import ua.nure.jfm.task4.packets.ShutdownRequestPacket;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler {
    private static Logger logger = Logger.getLogger("ClientHandler");

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
                logger.warning("Failed to read packet: " + e);
                continue;
            } catch (EOFException e) {
                logger.warning("Client " + login + " disconnected!");
                break;
            }

            log("got packet: " + packet + " of type " + packet.getPacketType());
            if(packet instanceof SendMessagePacket messagePacket) {
                server.clientSentMessage(this, messagePacket.text);
            } else if(packet instanceof ShutdownRequestPacket shutdownPacket) {
                server.clientSentShutdown(this, shutdownPacket.password);
            }
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
        } catch (IOException ignored) {
        }
    }

    synchronized public void send(BasePacket packet) throws IOException {
        writer.write(packet.getPacketType().ordinal());
        for(byte byt : packet.encode()) { // WHY????
            writer.write(byt);
        }
        writer.flush();
    }

    public void log(String message) {
        String prefix = String.format("Client (%s:%d%s)",
                socket.getInetAddress(), socket.getLocalPort(), (login != null ? (", " + login) : ""));
        logger.info(prefix + ": " + message);
    }
}
