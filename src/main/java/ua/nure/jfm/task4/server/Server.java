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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class Server {
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %4$s %2$s %5$s%6$s%n");
    }

    private static final Logger logger = Logger.getLogger("Server");

    private final String address;
    private final int port;
    private ServerSocket socket;
    private Map<String, ClientHandler> clients = new HashMap<>();
    private final Properties users = new Properties();
    private String shutdownPassword = null;
    private boolean stopped = false;
    private final ConcurrentLinkedQueue<BasePacket> queue = new ConcurrentLinkedQueue<>();
    private final Object QUEUE_PUSH = new Object();
    private final Object LOOP_THREAD_EXIT = new Object();
    private Thread loopThread;

    public Server(String address, int port) {
        this.address = address;
        this.port = port;

        InputStream stream = getClass().getClassLoader().getResourceAsStream("users.properties");
        if (stream == null) {
            logger.warning("Failed to load users: stream is null");
        } else {
            try {
                users.load(stream);
                stream.close();
            } catch (IOException e) {
                logger.warning("Failed to load users: " + e);
            }
        }
    }

    public void run() throws IOException {
        if (socket != null) {
            throw new IllegalStateException("Server is already running!");
        }

        try {
            socket = new ServerSocket(port, 128, InetAddress.getByName(address));
            loopThread = new Thread(this::serverLoop);
            loopThread.setName("ServerLoop");
            loopThread.start();

            while (socket != null && !socket.isClosed()) {
                Socket clientSocket;
                try {
                    clientSocket = socket.accept();
                    logger.info("Client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getLocalPort());
                } catch (IOException e) {
                    logger.warning("Failed to accept client: " + e);
                    continue;
                }

                new Thread(() -> acceptClient(clientSocket)).start();
            }
        } finally {
            clients.clear();
            socket = null;
        }
    }

    public void stop() throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IllegalStateException("Server is not running!");
        }

        stopped = true;
        synchronized (QUEUE_PUSH) {
            QUEUE_PUSH.notifyAll();
        }
        synchronized (LOOP_THREAD_EXIT) {
            try {
                LOOP_THREAD_EXIT.wait();
            } catch (InterruptedException ignored) {
            }
        }

        Map<String, ClientHandler> clients = this.clients;
        this.clients = new HashMap<>();
        for (ClientHandler client : clients.values()) {
            try {
                client.send(new ServerStoppingPacket());
            } catch (IOException e) {
                logger.warning("Failed to send SERVER_STOPPING packet to client: " + e);
            }

            client.close();
        }

        socket.close();
        socket = null;
    }

    private void acceptClient(Socket clientSocket) {
        String login;
        try {
            ClientHandler client = new ClientHandler(this, clientSocket);
            BasePacket packet = client.readPacket();
            if (!(packet instanceof LoginPacket loginPacket)) {
                client.log("failed to authenticate: packet is not LoginPacket");
                client.send(new ServerErrorPacket(400, "Expected LoginPacket to be first!"));
                client.close();
                return;
            }

            if (!users.containsKey(loginPacket.login) || !users.get(loginPacket.login).equals(loginPacket.password)) {
                client.log("failed to authenticate: invalid login or password");
                client.send(new ServerErrorPacket(401, "Invalid login or password!"));
                client.close();
                return;
            }

            if (clients.containsKey(loginPacket.login)) {
                ClientHandler oldClient = clients.get(loginPacket.login);
                clients.remove(loginPacket.login);
                try {
                    client.log("disconnecting because another client with same login is connecting");
                    oldClient.send(new ServerErrorPacket(403, "New client with same login is connecting."));
                } catch (IOException e) {
                    logger.warning("Failed to send " + packet.getPacketType() + " packet to old client: " + e);
                }
                oldClient.close();
            }

            login = loginPacket.login;
            client.setLogin(login);
            client.send(new ServerHelloPacket());
            clients.put(loginPacket.login, client);
            client.handle();
        } catch (IOException e) {
            logger.warning("Failed to authenticate client: " + e);
            return;
        } catch (EOFException e) {
            logger.warning("Failed to authenticate client: Disconnected");
            return;
        }

        queuePush(new ClientConnectedPacket(login));
    }

    private void serverLoop() {
        while (true) {
            BasePacket packet = queue.poll();
            if (packet == null && stopped) {
                break;
            } else if (packet == null) {
                synchronized (QUEUE_PUSH) {
                    try {
                        QUEUE_PUSH.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                continue;
            }
            broadcast(packet);
        }

        loopThread = null;
        synchronized (LOOP_THREAD_EXIT) {
            LOOP_THREAD_EXIT.notifyAll();
        }
    }

    private void queuePush(BasePacket packet) {
        if (stopped) {
            return;
        }

        queue.add(packet);
        synchronized (QUEUE_PUSH) {
            QUEUE_PUSH.notifyAll();
        }
    }

    protected void broadcast(BasePacket packet) {
        if (socket == null || socket.isClosed()) {
            return;
        }

        logger.info("Broadcasting " + packet.getPacketType() + " to " + clients.size() + " clients");
        for (ClientHandler client : clients.values()) {
            try {
                client.send(packet);
            } catch (IOException e) {
                client.log("failed to send " + packet.getPacketType() + " packet: " + e);
            }
        }
    }

    protected synchronized void clientDisconnected(ClientHandler client) {
        if (clients.containsKey(client.getLogin()) && clients.get(client.getLogin()) == client) {
            clients.remove(client.getLogin());
        }

        queuePush(new ClientDisconnectedPacket(client.getLogin()));
    }

    protected synchronized void clientSentMessage(ClientHandler client, String message) {
        queuePush(new NewMessagePacket(client.getLogin(), message));
    }

    protected synchronized void clientSentShutdown(ClientHandler client, String password) {
        if (!shutdownPassword.equals(password)) {
            try {
                client.send(new ServerErrorPacket(403, "Shutdown password is incorrect!"));
            } catch (IOException e) {
                client.log("failed to send SERVER_ERROR packet: " + e);
            }
            client.close();
            return;
        }

        try {
            stop();
        } catch (IOException e) {
            logger.warning("Failed to stop server: " + e);
        }
    }

    public void setShutdownPassword(String shutdownPassword) {
        this.shutdownPassword = shutdownPassword;
    }
}
