package ua.nure.jfm.task4.packets;

import java.io.BufferedReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ServerErrorPacket extends BasePacket {
    public int code;
    public String message;

    private ServerErrorPacket() {
        code = 0;
        message = "";
    }

    public ServerErrorPacket(int code, String message) {
        checkStringSize(message);

        this.code = code;
        this.message = message;
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
    public BasePacket decode(BufferedReader reader) {
        return null;
    }
}
