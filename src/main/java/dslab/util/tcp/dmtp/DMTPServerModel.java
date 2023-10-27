package dslab.util.tcp.dmtp;

import dslab.data.OkPacket;
import dslab.data.Packet;
import dslab.data.QuitPacket;
import dslab.util.tcp.exceptions.PacketHandleException;
import dslab.util.tcp.annotations.CommandPacketHandler;
import dslab.data.dmtp.*;
import dslab.util.Message;
import dslab.util.tcp.PacketProtocol;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A class that models the flow of a DMTP connection
 */
public class DMTPServerModel extends PacketProtocol {

    private Message message = null;
    private Message getMessage() throws PacketHandleException {
        if(message == null) throw new PacketHandleException("");
        return message;
    }

    private Consumer<Message> onMessageSent;

    private Function<List<String>, ValidatedRecipients> validateRecipients;

    public static class ValidatedRecipients {
        public List<String> validRecipients = new ArrayList<>();
        public List<String> ignoredRecipients = new ArrayList<>();
        public List<String> erroredRecipients = new ArrayList<>();
    }

    /**
     * Sets the callback that is executed as soon as a client submitted a message
     * @param callback a consumer that takes the newly created message as argument
     */
    public void setOnMessageSent(Consumer<Message> callback){
        this.onMessageSent = callback;
    }

    /**
     * Sets the function that validates recioients of a message
     * @param validator the validator function that accespts the list of
     *                  recipients and returns those that are invalid.
     *                  null or an empty list mean that all recipients were valid.
     */
    public void setRecipientValidator(Function<List<String>, ValidatedRecipients> validator){
        this.validateRecipients = validator;
    }

    @CommandPacketHandler
    public void handleBegin(BeginPacket packet) throws PacketHandleException {
        if(message != null) throw new PacketHandleException("");

        message = new Message();
        // TODO remove
        //message.sender = "general.kenobi@jedi.coruscant";
        //message.recipients = List.of("general.grievous@dookus-gang.seperatists");
        //message.subject = "You are smaller than i expected ;)";
        //message.message = "Hello there!\n- General Kenobi! You are a bold one.";
    }

    @CommandPacketHandler
    public void handleData(MessagePacket packet) throws PacketHandleException {
        getMessage().message = packet.message;
    }

    @CommandPacketHandler
    public void handleSender(SenderPacket packet) throws PacketHandleException {
        getMessage().sender = packet.sender;
    }

    @CommandPacketHandler
    public OkPacket handleRecipients(ReceiverPacket packet) throws PacketHandleException {

        if(validateRecipients != null) {
            var validated = validateRecipients.apply(packet.recipients);
            if(validated.erroredRecipients.size() > 0) throw new PacketHandleException("");
            getMessage().recipients = validated.validRecipients;
        }
        else getMessage().recipients = packet.recipients;

        return new OkPacket().withMessage(getMessage().recipients.size() + "");
    }

    @CommandPacketHandler
    public void handleSubject(SubjectPacket packet) throws PacketHandleException {
        getMessage().subject = packet.subject;
    }

    @CommandPacketHandler
    public void handleSend(SendPacket packet) throws PacketHandleException {
        var message = getMessage();
        if(message.sender == null) throw new PacketHandleException("no sender");
        if(message.subject == null) throw new PacketHandleException("no subject");
        if(message.message == null) throw new PacketHandleException("no data");
        if(message.recipients == null) throw new PacketHandleException("no recipients");
        this.message = null;
        if(onMessageSent != null) onMessageSent.accept(message);
    }

    @CommandPacketHandler
    public void handleQuit(QuitPacket packet) throws ProtocolCloseException {
        throw new ProtocolCloseException(new OkPacket().withMessage("bye"));
    }

    @Override
    protected boolean protocolErrorIsFatal() {
        return true;
    }

    @Override
    public String protocolName() {
        return "DMTP";
    }
}
