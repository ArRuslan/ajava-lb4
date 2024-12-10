package ua.nure.jfm.task4.packets;

import ua.nure.jfm.task4.exceptions.EOFException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class NewMessagePacket extends BasePacket {
    public String login;
    public String text;

    NewMessagePacket() {
        login = "";
        text = "";
    }

    public NewMessagePacket(String login, String text) {
        checkStringSize(login);
        checkStringSize(text);

        this.login = login;
        this.text = text;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.NEW_MESSAGE;
    }

    @Override
    public byte[] encode() {
        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[STRING_LENGTH_SIZE + loginBytes.length + STRING_LENGTH_SIZE + textBytes.length];
        ByteBuffer buf = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
        buf.putChar((char) loginBytes.length);
        buf.put(loginBytes);
        buf.putChar((char) textBytes.length);
        buf.put(textBytes);

        return result;
    }

    @Override
    public void decode(BufferedReader reader) throws IOException, EOFException {
        byte[] tmp = readExactlyBytesWithEOF(reader, STRING_LENGTH_SIZE);
        ByteBuffer buf = ByteBuffer.wrap(tmp).order(ByteOrder.LITTLE_ENDIAN);
        char loginSize = buf.getChar();

        tmp = readExactlyBytesWithEOF(reader, loginSize);
        login = new String(tmp);

        tmp = readExactlyBytesWithEOF(reader, STRING_LENGTH_SIZE);
        buf = ByteBuffer.wrap(tmp).order(ByteOrder.LITTLE_ENDIAN);
        char textSize = buf.getChar();

        tmp = readExactlyBytesWithEOF(reader, textSize);
        text = new String(tmp);
    }
}
