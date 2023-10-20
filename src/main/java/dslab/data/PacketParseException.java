package dslab.data;

public class PacketParseException extends IllegalArgumentException {
    public PacketParseException() {
        super("CData packet was in invalid format and could not be parsed.");
    }
}
