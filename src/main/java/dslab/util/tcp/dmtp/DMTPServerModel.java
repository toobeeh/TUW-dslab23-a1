package dslab.util.tcp.dmtp;

import dslab.data.exceptions.PacketHandleException;
import dslab.data.annotations.CommandPacketHandler;
import dslab.data.dmtp.*;
import dslab.util.Message;
import dslab.util.tcp.PacketProtocol;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.util.List;
import java.util.function.Consumer;
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

    private UnaryOperator<List<String>> validateRecipients;

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
    public void setRecipientValidator(UnaryOperator<List<String>> validator){
        this.validateRecipients = validator;
    }

    @CommandPacketHandler
    public void handleBegin(BeginPacket packet) throws PacketHandleException {
        if(message != null) throw new PacketHandleException("");
        message = new Message();
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
    public void handleRecipients(ReceiverPacket packet) throws PacketHandleException {
        if(validateRecipients != null) {
            var invalid = validateRecipients.apply(packet.recipients);
            if(invalid != null && invalid.size() > 0){
                var error = "unknown user" + (invalid.size() > 1 ? "s" : "") + " "
                        + String.join(",", invalid);
                throw new PacketHandleException(error);
            }
        }
        getMessage().recipients = packet.recipients;
    }

    @CommandPacketHandler
    public void handleSubject(SubjectPacket packet) throws PacketHandleException {
        getMessage().subject = packet.subject;
    }

    @CommandPacketHandler
    public void handleSend(SendPacket packet) throws PacketHandleException {

        // TODO remove
        if(this.message == null) {
            var m = new Message();
            m.sender = "test@earth.planet";
            m.recipients = List.of("zaphod@earth.planet");
            m.subject = "testsubject";
            m.message = "testdata";
            this.message = m;
        }

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
}
