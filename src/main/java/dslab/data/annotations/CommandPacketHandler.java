package dslab.data.annotations;

import dslab.data.Packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that registers a method as command packet handler.
 * The method must take exactly one argument of a class that
 * implements Packet and is annotated with @CommandPacket.
 *
 * The method can either return void (default response string of the
 * packet is returned) or a custom String return value.
 */
@Retention(RetentionPolicy.RUNTIME) // This annotation should be retained at runtime
@Target(ElementType.METHOD) // This annotation can be applied to methods
public @interface CommandPacketHandler { }
