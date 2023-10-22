package dslab.transfer;

import dslab.data.exceptions.PacketHandleException;
import dslab.data.annotations.CommandPacketHandler;
import dslab.data.dmtp.*;
import dslab.util.tcp.PacketProtocol;

import java.util.List;

class Message {
    String subject;
    String sender;
    String message;
    List<String> recipients;
}

public class DMTP extends PacketProtocol {
    private Message message = null;
    private Message getMessage() throws PacketHandleException {
        if(this.message == null) throw new PacketHandleException("has not begun");
        return message;
    }

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
        var message = this.getMessage();
        if(message.sender == null) throw new PacketHandleException("no sender");
        if(message.subject == null) throw new PacketHandleException("no subject");
        if(message.message == null) throw new PacketHandleException("no data");
        if(message.recipients == null) throw new PacketHandleException("no recipients");
        this.message = null;
    }

    @CommandPacketHandler
    public String handleQuit(QuitPacket packet) {
        // quit
        return "example custom return";
    }
}
