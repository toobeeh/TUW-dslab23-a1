package dslab.data.annotations;

import dslab.data.PacketFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks a factory class for a specific packet
 */
@Retention(RetentionPolicy.RUNTIME) // This annotation should be retained at runtime
@Target(ElementType.TYPE) // This annotation can be applied to classes
public @interface CommandPacketFactory {
}
