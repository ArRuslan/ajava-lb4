package ua.nure.jfm.task4.packets;

import ua.nure.jfm.task4.exceptions.EOFException;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerHelloPacket extends BasePacket {
    @Override
    public PacketType getPacketType() {
        return PacketType.SERVER_HELLO;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public void decode(BufferedReader reader) throws IOException, EOFException {

    }
}
