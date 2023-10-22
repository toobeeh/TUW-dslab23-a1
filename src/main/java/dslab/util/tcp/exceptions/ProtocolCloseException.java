package dslab.util.tcp.exceptions;

public class ProtocolCloseException extends Exception {
    private String response;
    public ProtocolCloseException(String response) {
        super("Protocol connection has been closed");
        this.response = response;
    }
    public String getResponseString() { return this.response; }
}
