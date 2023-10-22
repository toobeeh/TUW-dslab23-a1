package dslab.util.dns;

/**
 * An exception that occurs when a requested domain name could not be found
 */
public class DomainNameNotFoundException extends Exception {
    public DomainNameNotFoundException() {
        super("The requested domain name could not be found");
    }
}
