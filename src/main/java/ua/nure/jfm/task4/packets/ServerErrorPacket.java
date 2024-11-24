package ua.nure.jfm.task4.packets;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ServerErrorPacket extends BasePacket {
    public int code;
    public String message;

    ServerErrorPacket() {
        code = 0;
        message = "";
    }

    public ServerErrorPacket(int code, String message) {
        checkStringSize(message);

        this.code = code;
        this.message = message;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.SERVER_ERROR;
    }

    @Override
    public byte[] encode() {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[INT_SIZE + STRING_LENGTH_SIZE + messageBytes.length];
        ByteBuffer buf = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(code);
        buf.putChar((char)messageBytes.length);
        buf.put(messageBytes);

        return result;
    }

    @Override
    public void decode(BufferedReader reader) throws IOException {
        char[] tmp = new char[INT_SIZE];
        if(!readExactly(reader, tmp)) {
            throw new IOException("EOF");
        }
        ByteBuffer buf = ByteBuffer.wrap(charArrToByteArr(tmp)).order(ByteOrder.LITTLE_ENDIAN);
        code = buf.getInt();

        tmp = new char[STRING_LENGTH_SIZE];
        if(!readExactly(reader, tmp)) {
            throw new IOException("EOF");
        }

        buf = ByteBuffer.wrap(charArrToByteArr(tmp)).order(ByteOrder.LITTLE_ENDIAN);
        char messageSize = buf.getChar();

        tmp = new char[messageSize];
        if(!readExactly(reader, tmp)) {
            throw new IOException("EOF");
        }
        message = new String(tmp);
    }
}
