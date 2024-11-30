package ua.nure.jfm.task4.packets;

import ua.nure.jfm.task4.exceptions.EOFException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ClientConnectedPacket extends BasePacket {
    public String login;

    ClientConnectedPacket() {
        login = "";
    }

    public ClientConnectedPacket(String login) {
        this.login = login;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.CLIENT_CONNECTED;
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
        byte[] tmp = readExactlyBytesWithEOF(reader, STRING_LENGTH_SIZE);

        ByteBuffer buf = ByteBuffer.wrap(tmp).order(ByteOrder.LITTLE_ENDIAN);
        char messageSize = buf.getChar();

        tmp = readExactlyBytesWithEOF(reader, messageSize);
        login = new String(tmp);
    }
}
