package dslab.data.annotations;

import dslab.data.Packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // This annotation should be retained at runtime
@Target(ElementType.METHOD) // This annotation can be applied to methods
public @interface ProcessCommandPacket {
    Class<? extends Packet> value(); // The attribute to store the identification name
}
