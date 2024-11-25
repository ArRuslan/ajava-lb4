package ua.nure.jfm.task4.packets;

import ua.nure.jfm.task4.exceptions.EOFException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ShutdownRequestPacket extends BasePacket {
    public String password;

    ShutdownRequestPacket() {
        password = "";
    }

    public ShutdownRequestPacket(String text) {
        checkStringSize(text);

        this.password = text;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.SHUTDOWN_REQUEST;
    }

    @Override
    public byte[] encode() {
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[STRING_LENGTH_SIZE + passwordBytes.length];
        ByteBuffer buf = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
        buf.putChar((char)passwordBytes.length);
        buf.put(passwordBytes);

        return result;
    }

    @Override
    public void decode(BufferedReader reader) throws IOException, EOFException {
        char[] tmp = new char[STRING_LENGTH_SIZE];
        if(!readExactly(reader, tmp)) {
            throw new EOFException();
        }

        ByteBuffer buf = ByteBuffer.wrap(charArrToByteArr(tmp)).order(ByteOrder.LITTLE_ENDIAN);
        char loginSize = buf.getChar();

        tmp = new char[loginSize];
        if(!readExactly(reader, tmp)) {
            throw new EOFException();
        }
        password = new String(tmp);
    }
}
