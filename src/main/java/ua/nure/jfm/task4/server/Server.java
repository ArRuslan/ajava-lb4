package ua.nure.jfm.task4.server;

import ua.nure.jfm.task4.exceptions.EOFException;
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
            try {
                client.send(new ServerStoppingPacket());
            } catch (IOException e) {
                System.err.println("Failed to send SERVER_STOPPING packet to client: " + e);
            }

            client.close();
        }

        socket.close();
    }

    private void acceptClient() {
        if(socket == null) {
            throw new IllegalStateException("Server is not running!");
        }

        String login = "";
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

            login = loginPacket.login;
            client.setLogin(login);
            client.send(new ServerHelloPacket());
            clients.put(loginPacket.login, client);
            client.handle();
        } catch (IOException e) {
            System.err.println("Failed to authenticate client: " + e);
        } catch (EOFException e) {
            System.err.println("Failed to authenticate client: Disconnected");
            return;
        }

        broadcast(new ClientConnectedPacket(login));
    }

    protected void broadcast(BasePacket packet) {
        for(ClientHandler client : clients.values()) {
            try {
                client.send(packet);
            } catch (IOException e) {
                System.err.println("Failed to send " + packet.getPacketType() + " packet to client: " + e);
            }
        }
    }

    protected synchronized void clientDisconnected(ClientHandler client) {
        if(clients.containsKey(client.getLogin()) && clients.get(client.getLogin()) == client) {
            clients.remove(client.getLogin());
        }

        broadcast(new ClientDisconnectedPacket(client.getLogin()));
    }

    protected synchronized void clientSentMessage(ClientHandler client, String message) {
        broadcast(new NewMessagePacket(client.getLogin(), message));
    }
}
