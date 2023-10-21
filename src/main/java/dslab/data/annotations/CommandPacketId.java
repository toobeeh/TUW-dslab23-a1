package dslab.data.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // This annotation should be retained at runtime
@Target(ElementType.TYPE) // This annotation can be applied to classes
public @interface CommandPacketId {
    String value(); // The attribute to store the identification name
}
