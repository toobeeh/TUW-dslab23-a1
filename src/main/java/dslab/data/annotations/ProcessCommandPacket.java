package dslab.data.annotations;

import dslab.data.Packet;

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
@Target(ElementType.METHOD) // This annotation can be applied to methods
public @interface ProcessCommandPacket {
    Class<? extends Packet> value(); // The attribute to store the identification name
}
