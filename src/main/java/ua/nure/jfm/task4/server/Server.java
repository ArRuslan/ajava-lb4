package ua.nure.jfm.task4.server;

import ua.nure.jfm.task4.exceptions.EOFException;
import ua.nure.jfm.task4.packets.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Server {
    private final String address;
    private final int port;
    private ServerSocket socket;
    private final Map<String, ClientHandler> clients = new HashMap<>();
    private final Properties users = new Properties();
    private String shutdownPassword = null;

    public Server(String address, int port) {
        this.address = address;
        this.port = port;

        InputStream stream = getClass().getClassLoader().getResourceAsStream("users.properties");
        if (stream == null) {
            System.err.println("Failed to load users: stream is null");
        } else {
            try {
                users.load(stream);
                stream.close();
            } catch (IOException e) {
                System.err.println("Failed to load users: " + e);
            }
        }
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
        socket = null;
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

            if(!users.containsKey(loginPacket.login) || !users.get(loginPacket.login).equals(loginPacket.password)) {
                client.send(new ServerErrorPacket(401, "Invalid login or password!"));
                client.close();
                return;
            }

            if(clients.containsKey(loginPacket.login)) {
                ClientHandler oldClient = clients.get(loginPacket.login);
                clients.remove(loginPacket.login);
                try {
                    oldClient.send(new ServerErrorPacket(403, "New client with same login is connecting."));
                } catch (IOException e) {
                    System.err.println("Failed to send " + packet.getPacketType() + " packet to old client: " + e);
                }
                oldClient.close();
            }

            login = loginPacket.login;
            client.setLogin(login);
            client.send(new ServerHelloPacket());
            clients.put(loginPacket.login, client);
            client.handle();
        } catch (IOException e) {
            System.err.println("Failed to authenticate client: " + e);
            return;
        } catch (EOFException e) {
            System.err.println("Failed to authenticate client: Disconnected");
            return;
        }

        broadcast(new ClientConnectedPacket(login));
    }

    protected void broadcast(BasePacket packet) {
        System.out.println("Broadcasting "+packet.getPacketType()+" to " + clients.size() + " clients");
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

    protected synchronized void clientSentShutdown(ClientHandler client, String password) {
        if(!shutdownPassword.equals(password)) {
            try {
                client.send(new ServerErrorPacket(403, "Shutdown password is incorrect!"));
            } catch (IOException e) {
                System.err.println("Failed to send SERVER_ERROR packet to client: " + e);
            }
            client.close();
            return;
        }

        try {
            stop();
        } catch (IOException e) {
            System.err.println("Failed to stop server: " + e);
        }
    }

    public void setShutdownPassword(String shutdownPassword) {
        this.shutdownPassword = shutdownPassword;
    }
}
