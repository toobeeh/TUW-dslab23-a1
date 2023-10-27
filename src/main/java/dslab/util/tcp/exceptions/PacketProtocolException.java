package dslab.util.tcp.exceptions;

/**
 * An exception that occurs when a received packet is not part of the protocol
 */
public class PacketProtocolException extends Exception {
    public PacketProtocolException() {
        super("The received packet was not part of this protocol");
    }
}
