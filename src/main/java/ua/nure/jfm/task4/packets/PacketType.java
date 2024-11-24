package ua.nure.jfm.task4.packets;

public enum PacketType {
    SERVER_ERROR,

    LOGIN,
    SERVER_HELLO,

    CLIENT_CONNECTED,
    CLIENT_DISCONNECTED,

    SEND_MESSAGE,
    NEW_MESSAGE,

    SERVER_STOPPING,
}
