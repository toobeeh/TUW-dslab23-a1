package dslab.util.tcp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that gives a packet an identifier.
 * This identifier should be the token which starts the commands in the
 * used protocol.
 */
@Retention(RetentionPolicy.RUNTIME) // This annotation should be retained at runtime
@Target(ElementType.TYPE) // This annotation can be applied to classes
public @interface CommandPacket {
    String value(); // The command name of the packet (first index of content.split(" "))
}
