package dslab.data;

/**
 * An exception that occurs while handling a valid packet;
 * eg when it has been sent in an illeagal state or contains illegal data
 */
public class PacketProcessException extends Exception {
    private String response;
    public PacketProcessException(String response) {
        super("An error occurred while processing the packet");
        this.response = response;
    }
    public String getResponseString() { return this.response; }
}
