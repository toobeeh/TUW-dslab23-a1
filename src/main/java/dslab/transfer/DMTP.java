package dslab.transfer;

import dslab.data.PacketProcessException;
import dslab.data.annotations.ProcessCommandPacket;
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
    private Message getMessage() throws PacketProcessException {
        if(this.message == null) throw new PacketProcessException("has not begun");
        return message;
    }

    @ProcessCommandPacket(BeginPacket.class)
    public void processBegin(BeginPacket packet) throws PacketProcessException {
        if(message != null) throw new PacketProcessException("has already begun");
        message = new Message();
    }

    @ProcessCommandPacket(MessagePacket.class)
    public void processData(MessagePacket packet) throws PacketProcessException {
        this.getMessage().message = packet.message;
    }

    @ProcessCommandPacket(SenderPacket.class)
    public void processSender(SenderPacket packet) throws PacketProcessException {
        this.getMessage().sender = packet.sender;
    }

    @ProcessCommandPacket(ReceiverPacket.class)
    public void processRecipients(ReceiverPacket packet) throws PacketProcessException {
        this.getMessage().recipients = packet.recipients;
    }

    @ProcessCommandPacket(SubjectPacket.class)
    public void processSubject(SubjectPacket packet) throws PacketProcessException {
        this.getMessage().subject = packet.subject;
    }

    @ProcessCommandPacket(SendPacket.class)
    public void processSend(SendPacket packet) throws PacketProcessException {
        var message = this.getMessage();
        if(message.sender == null) throw new PacketProcessException("no sender");
        if(message.subject == null) throw new PacketProcessException("no subject");
        if(message.message == null) throw new PacketProcessException("no data");
        if(message.recipients == null) throw new PacketProcessException("no recipients");
        this.message = null;
    }

    @ProcessCommandPacket(QuitPacket.class)
    public String processQuit(QuitPacket packet) {
        // quit
        return "example custom return";
    }
}
