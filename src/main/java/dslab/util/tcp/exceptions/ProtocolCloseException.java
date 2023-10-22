package dslab.util.tcp.exceptions;

import dslab.data.Packet;

public class ProtocolCloseException extends Exception {
    private Packet response;
    public ProtocolCloseException(Packet response) {
        super("Protocol connection has been closed");
        this.response = response;
    }
    public Packet getResponsePacket() { return this.response; }
}
