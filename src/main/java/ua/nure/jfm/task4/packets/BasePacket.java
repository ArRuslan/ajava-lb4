package ua.nure.jfm.task4.packets;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BasePacket {
    public int INT_SIZE = 4;
    public int STRING_LENGTH_SIZE = 2;

    abstract public byte[] encode();
    abstract public BasePacket decode(BufferedReader reader);

    protected static void checkStringSize(String string) {
        if(string.getBytes(StandardCharsets.UTF_8).length > 0x7fff) {
            throw new IllegalArgumentException("String length must be less than 32767 bytes");
        }
    }

    public static BasePacket readPacket(BufferedReader reader) throws IOException {
        char[] data = new char[1];
        int read;
        while((read = reader.read(data, 0, 1)) == 0);
        if(read == -1) {
            throw new IOException("EOF");
        }

        if(data[0] > PacketType.values().length) {
            throw new IllegalArgumentException("Invalid packet type: "+ data[0]);
        }

        PacketType type = PacketType.values()[data[0]];
        BasePacket packet = null;

        switch (type) {
            case SERVER_ERROR: {
                ServerErrorPacket packet_ = new ServerErrorPacket();
                packet = packet_;
                break;
            }
            case LOGIN: {
                LoginPacket packet_ = new LoginPacket();
                packet = packet_;
                break;
            }
            case SERVER_HELLO: {
                ServerHelloPacket packet_ = new ServerHelloPacket();
                packet = packet_;
                break;
            }
            case CLIENT_CONNECTED: {
                ClientConnectedPacket packet_ = new ClientConnectedPacket();
                packet = packet_;
                break;
            }
            case CLIENT_DISCONNECTED: {
                ClientDisconnectedPacket packet_ = new ClientDisconnectedPacket();
                packet = packet_;
                break;
            }
            case SEND_MESSAGE: {
                SendMessagePacket packet_ = new SendMessagePacket();
                packet = packet_;
                break;
            }
            case NEW_MESSAGE: {
                NewMessagePacket packet_ = new NewMessagePacket();
                packet = packet_;
                break;
            }
            case SERVER_STOPPING: {
                ServerStoppingPacket packet_ = new ServerStoppingPacket();
                packet = packet_;
                break;
            }
        }

        return packet;
    }
}
