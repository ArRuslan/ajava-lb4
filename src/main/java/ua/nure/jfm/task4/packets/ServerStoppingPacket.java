package ua.nure.jfm.task4.packets;

import java.io.BufferedReader;

public class ServerStoppingPacket extends BasePacket {
    @Override
    public PacketType getPacketType() {
        return PacketType.SERVER_STOPPING;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public void decode(BufferedReader reader) {

    }
}
