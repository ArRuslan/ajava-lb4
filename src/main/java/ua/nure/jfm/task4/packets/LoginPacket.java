package ua.nure.jfm.task4.packets;

import ua.nure.jfm.task4.exceptions.EOFException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class LoginPacket extends BasePacket {
    public String login;
    public String password;

    LoginPacket() {
        login = "";
        password = "";
    }

    public LoginPacket(String login, String password) {
        checkStringSize(login);
        checkStringSize(password);

        this.login = login;
        this.password = password;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.LOGIN;
    }

    @Override
    public byte[] encode() {
        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[STRING_LENGTH_SIZE + loginBytes.length + STRING_LENGTH_SIZE + passwordBytes.length];
        ByteBuffer buf = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
        buf.putChar((char)loginBytes.length);
        buf.put(loginBytes);
        buf.putChar((char)passwordBytes.length);
        buf.put(passwordBytes);

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
        char passwordSize = buf.getChar();

        tmp = readExactlyBytesWithEOF(reader, passwordSize);
        password = new String(tmp);
    }
}
