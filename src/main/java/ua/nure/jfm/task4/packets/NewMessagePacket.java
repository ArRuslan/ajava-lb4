package ua.nure.jfm.task4.packets;

import java.io.BufferedReader;

public class NewMessagePacket extends BasePacket {
    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    protected BasePacket decode(BufferedReader reader) {
        return null;
    }
}
