package ua.nure.jfm.task4.packets;

import ua.nure.jfm.task4.exceptions.EOFException;

import java.io.BufferedReader;
import java.io.IOException;

public class NewMessagePacket extends BasePacket {
    @Override
    public PacketType getPacketType() {
        return PacketType.NEW_MESSAGE;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public void decode(BufferedReader reader) throws IOException, EOFException {

    }
}
