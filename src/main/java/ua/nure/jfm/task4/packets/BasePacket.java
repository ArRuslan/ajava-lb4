package ua.nure.jfm.task4.packets;

import ua.nure.jfm.task4.exceptions.EOFException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BasePacket {
    public int INT_SIZE = 4;
    public int STRING_LENGTH_SIZE = 2;

    abstract public PacketType getPacketType();
    abstract public byte[] encode();
    abstract public void decode(BufferedReader reader) throws IOException, EOFException;

    protected static void checkStringSize(String string) {
        if(string.getBytes(StandardCharsets.UTF_8).length > 0x7fff) {
            throw new IllegalArgumentException("String length must be less than 32767 bytes");
        }
    }

    protected static boolean readExactly(BufferedReader reader, char[] out) throws IOException {
        int totalRead = 0;
        int read = reader.read(out);
        totalRead += read;
        while(read != -1 && totalRead < out.length) {
            read = reader.read(out, totalRead, out.length - totalRead);
            totalRead += read;
        }

        return read != -1;
    }

    protected static byte[] charArrToByteArr(char[] arr) {
        byte[] result = new byte[arr.length];
        for(int i = 0; i < arr.length; i++) {
            result[i] = (byte)arr[i];
        }

        return result;
    }

    public static BasePacket readPacket(BufferedReader reader) throws IOException, EOFException {
        char[] data = new char[1];
        if(!readExactly(reader, data)) {
            throw new EOFException();
        }

        if(data[0] > PacketType.values().length) {
            throw new IllegalArgumentException("Invalid packet type: "+ data[0]);
        }

        PacketType type = PacketType.values()[data[0]];
        BasePacket packet = null;

        switch (type) {
            case SERVER_ERROR: {
                packet = new ServerErrorPacket();
                break;
            }
            case LOGIN: {
                packet = new LoginPacket();
                break;
            }
            case SERVER_HELLO: {
                packet = new ServerHelloPacket();
                break;
            }
            case CLIENT_CONNECTED: {
                packet = new ClientConnectedPacket();
                break;
            }
            case CLIENT_DISCONNECTED: {
                packet = new ClientDisconnectedPacket();
                break;
            }
            case SEND_MESSAGE: {
                packet = new SendMessagePacket();
                break;
            }
            case NEW_MESSAGE: {
                packet = new NewMessagePacket();
                break;
            }
            case SERVER_STOPPING: {
                packet = new ServerStoppingPacket();
                break;
            }
        }

        packet.decode(reader);
        return packet;
    }
}
