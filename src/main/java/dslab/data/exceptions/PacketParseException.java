package dslab.data.exceptions;

/**
 * An exception that occurs when parsing a packet;
 * indicates there is a handler for the packet, but
 * the contents were faulty
 */
public class PacketParseException extends Exception {
    private final String response;
    public PacketParseException(String response) {
        super("An error occurred while parsing the packet");
        this.response = response;
    }
    public PacketParseException() {
        this("parsing error");
    }
    public String getResponseString() { return this.response; }
}
