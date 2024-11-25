package ua.nure.jfm.task4.packets;

import ua.nure.jfm.task4.exceptions.EOFException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ClientDisconnectedPacket extends BasePacket {
    public String login;

    ClientDisconnectedPacket() {
        login = "";
    }

    public ClientDisconnectedPacket(String login) {
        this.login = login;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.CLIENT_DISCONNECTED;
    }

    @Override
    public byte[] encode() {
        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[STRING_LENGTH_SIZE + loginBytes.length];
        ByteBuffer buf = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
        buf.putChar((char)loginBytes.length);
        buf.put(loginBytes);

        return result;
    }

    @Override
    public void decode(BufferedReader reader) throws IOException, EOFException {
        char[] tmp = new char[STRING_LENGTH_SIZE];
        if(!readExactly(reader, tmp)) {
            throw new EOFException();
        }

        ByteBuffer buf = ByteBuffer.wrap(charArrToByteArr(tmp)).order(ByteOrder.LITTLE_ENDIAN);
        char messageSize = buf.getChar();

        tmp = new char[messageSize];
        if(!readExactly(reader, tmp)) {
            throw new EOFException();
        }
        login = new String(tmp);
    }
}
