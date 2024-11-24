package ua.nure.jfm.task4.server;

import ua.nure.jfm.task4.packets.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private final String address;
    private final int port;
    private ServerSocket socket;
    private final Map<String, ClientHandler> clients = new HashMap<>();

    public Server(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void run() throws IOException {
        if(socket != null) {
            throw new IllegalStateException("Server is already running!");
        }

        try {
            socket = new ServerSocket(port, 128, InetAddress.getByName(address));
            while(!socket.isClosed()) {
                acceptClient();
            }
        } finally {
            clients.clear();
            socket = null;
        }
    }

    public void stop() throws IOException {
        if(socket == null || socket.isClosed()) {
            throw new IllegalStateException("Server is not running!");
        }

        for(ClientHandler client : clients.values()) {
            client.send(new ServerStoppingPacket());
            client.close();
        }

        socket.close();
    }

    private void acceptClient() {
        if(socket == null) {
            throw new IllegalStateException("Server is not running!");
        }

        try {
            Socket clientSocket = socket.accept();
            ClientHandler client = new ClientHandler(this, clientSocket);
            BasePacket packet = client.readPacket();
            if(!(packet instanceof LoginPacket loginPacket)) {
                client.send(new ServerErrorPacket(400, "Expected LoginPacket to be first!"));
                client.close();
                return;
            }

            // TODO: get logins and passwords from file
            if(!loginPacket.login.equals("test") || !loginPacket.password.equals("test")) {
                client.send(new ServerErrorPacket(401, "Invalid login or password!"));
                client.close();
                return;
            }

            client.send(new ServerHelloPacket());
            clients.put(loginPacket.login, client);
            client.handle();
        } catch (IOException e) {
            System.err.println("Failed to authenticate client: "+e);
        }
    }
}
