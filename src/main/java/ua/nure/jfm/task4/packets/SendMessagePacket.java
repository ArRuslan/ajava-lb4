package ua.nure.jfm.task4.packets;

import ua.nure.jfm.task4.exceptions.EOFException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class SendMessagePacket extends BasePacket {
    public String text;

    SendMessagePacket() {
        text = "";
    }

    public SendMessagePacket(String text) {
        checkStringSize(text);

        this.text = text;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.SEND_MESSAGE;
    }

    @Override
    public byte[] encode() {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[STRING_LENGTH_SIZE + textBytes.length];
        ByteBuffer buf = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
        buf.putChar((char)textBytes.length);
        buf.put(textBytes);

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
        text = new String(tmp);
    }
}
