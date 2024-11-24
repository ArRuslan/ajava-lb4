package ua.nure.jfm.task4.packets;

import java.io.BufferedReader;

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
    public void decode(BufferedReader reader) {

    }
}
