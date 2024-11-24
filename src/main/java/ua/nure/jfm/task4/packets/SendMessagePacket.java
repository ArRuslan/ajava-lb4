package ua.nure.jfm.task4.packets;

import java.io.BufferedReader;

public class SendMessagePacket extends BasePacket {
    @Override
    public PacketType getPacketType() {
        return PacketType.SEND_MESSAGE;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public void decode(BufferedReader reader) {

    }
}
