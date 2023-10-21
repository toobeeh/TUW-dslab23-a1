package dslab.util.tcp;

import dslab.data.*;
import dslab.data.annotations.CommandPacketFactory;
import dslab.data.annotations.CommandPacketId;
import dslab.data.annotations.ProcessCommandPacket;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

class InvalidPacketException extends Exception {
    public InvalidPacketException() {
        super("The packet command did not match any known commands");
    }
}

/**
 * A class that provides comprehensive parsing of data packages.
 * Incoming packages contain only of a String; this class checks all provided handlers
 * (processor methods implemented in subclasses) to match the incoming data.
 * Then, the packet is parsed for the handler and the handler executed.
 *
 * The handler methods have to be implemented in the deriving class.
 * Each handler needs an annotation ProcessCommandPacket which
 * registers the handler specifically for this packet type.
 * Handlers need to return strings.
 *
 * Annotation type checks are performed extensively at runtime.
 */
public abstract class PacketParser {

    private HashMap<String, Method> packetHandlers = new HashMap<>();
    private HashMap<String, PacketFactory> packetFactories = new HashMap<>();

    private interface CommandProcessorMethod {
        public String commandProcessor(Packet packet);
    }

    public PacketParser(){

        // get packet handlers
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(ProcessCommandPacket.class)) {

                // check if method signature is valid
                validateCommandPacketProcessor(method);

                // get packet type that the method handles (annotation is present as of previous if statement)
                var processorAnnotation = method.getAnnotation(ProcessCommandPacket.class);
                Class<? extends Packet> packetType = processorAnnotation.value();


                // get the factory for that packet type
                Class<? extends PacketFactory> packetFactory = null;
                var classes = packetType.getClasses();
                for(Class clazz : classes){
                    if(clazz.getAnnotation(CommandPacketFactory.class) != null) packetFactory = clazz;
                }
                if(packetFactory == null) throw new RuntimeException("No factory annotation present for packet class");
                validateCommandPacketFactory(packetFactory, packetType);

                // get the identification of the packet
                var packetIdentification = packetType.getAnnotation(CommandPacketId.class);
                if(packetIdentification == null) throw new RuntimeException("No identification annotation present for packet class");
                String identification = packetIdentification.value();

                // register handler and factory
                this.packetFactories.put(identification, getFactoryInstance(packetFactory));
                this.packetHandlers.put(identification, method);
            }
        }
    }

    /**
     * Parse an incoming plain-text packet
     * @param data the packet text
     * @return the response, generated by the packet handler
     * @throws InvalidPacketException
     */
    public String parse(String data) throws InvalidPacketException {
        var packet = this.createPacketFromString(data);
        String identification = packet.getClass().getAnnotation(CommandPacketId.class).value(); // is present as of constructor checks

        // the handler should have the right signature as it is checked in the constructor
        var handler = this.packetHandlers.get(identification);
        if(handler == null) {
            System.err.println("Unknown packet");
            throw new InvalidPacketException();
        }

        String result;
        try {
            result = (String) handler.invoke(this, packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    /**
     * gets the right packet class for the incoming data and creates an packet instance
     * @param data incoming plain packet data string
     * @return the parsed packet object
     * @throws InvalidPacketException
     */
    private Packet createPacketFromString(String data) throws InvalidPacketException {
        String identification = data.split(" ")[0];
        var factory = this.packetFactories.get(identification);
        if(factory == null) {
            System.err.println("Unknown packet");
            throw new InvalidPacketException();
        }

        return factory.create(data);
    }

    /**
     * validates a found packet handler method
     * @param candidate a packet processor/handler method
     */
    private void validateCommandPacketProcessor(Method candidate){

        var paramTypes = candidate.getParameterTypes();
        if(paramTypes.length != 1 || paramTypes.length > 0 && !Packet.class.isAssignableFrom(paramTypes[0])){
            throw new IllegalArgumentException("packet processor must only have exactly one argument of type Packet");
        }
        if(!String.class.isAssignableFrom(candidate.getReturnType())){
            throw new IllegalArgumentException("packet processor must only return a string");
        }
    }

    /**
     * validates a factory class for a given packet type
     * @param candidate a factory class
     * @param packetType the packet which should be created by the factory class
     */
    private void validateCommandPacketFactory(Class<? extends PacketFactory> candidate, Class<? extends Packet> packetType) {

        if(!PacketFactory.class.isAssignableFrom(candidate)) throw new IllegalArgumentException("packet factory needs to implement packetfactory interface");

        try {
            var constructorParamTypes = candidate.getConstructor().getParameterTypes();
            if(constructorParamTypes.length != 0) throw new IllegalArgumentException("packet factory constructor must not have arguments");
        }
        catch(NoSuchMethodException e) {
            // thats okay, it just doesnt have to take params
        }

        try {
            var factoryMethod = candidate.getMethod("create", String.class);
            if(!packetType.isAssignableFrom(factoryMethod.getReturnType())) throw new IllegalArgumentException("packet factory method returns incompatible packet");
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("packet factory method could not be found");
        }
    }

    /**
     * creates a new instance of a factory class
     * @param clazz the factory class
     * @return the created instance
     */
    private PacketFactory getFactoryInstance(Class<? extends PacketFactory> clazz){
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
