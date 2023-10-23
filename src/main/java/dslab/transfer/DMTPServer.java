package dslab.transfer;

import dslab.data.exceptions.PacketHandleException;
import dslab.data.annotations.CommandPacketHandler;
import dslab.data.dmtp.*;
import dslab.util.tcp.PacketProtocol;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * A class that models the flow of a DMTP connection
 */
public class DMTPServer extends PacketProtocol {

    public class Message {
        public String subject;
        public String sender;
        public String message;
        public List<String> recipients;
    }
    private Message message = null;
    private Message getMessage() throws PacketHandleException {
        if(this.message == null) throw new PacketHandleException("has not begun");
        return message;
    }

    public Consumer<Message> onMessageSent;

    @CommandPacketHandler
    public void handleBegin(BeginPacket packet) throws PacketHandleException {
        if(message != null) throw new PacketHandleException("has already begun");
        message = new Message();
    }

    @CommandPacketHandler
    public void handleData(MessagePacket packet) throws PacketHandleException {
        this.getMessage().message = packet.message;
    }

    @CommandPacketHandler
    public void handleSender(SenderPacket packet) throws PacketHandleException {
        this.getMessage().sender = packet.sender;
    }

    @CommandPacketHandler
    public void handleRecipients(ReceiverPacket packet) throws PacketHandleException {
        this.getMessage().recipients = packet.recipients;
    }

    @CommandPacketHandler
    public void handleSubject(SubjectPacket packet) throws PacketHandleException {
        this.getMessage().subject = packet.subject;
    }

    @CommandPacketHandler
    public void handleSend(SendPacket packet) throws PacketHandleException {

        // TODO remove
        if(this.message == null) {
            var m = new Message();
            m.sender = "a";
            m.recipients = List.of("b");
            m.subject = "c";
            m.message = "d";
            this.message = m;
        }

        var message = this.getMessage();
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
