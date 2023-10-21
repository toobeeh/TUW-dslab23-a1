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
    public String processBegin(BeginPacket packet) throws PacketProcessException {
        if(message != null) throw new PacketProcessException("has already begun");
        message = new Message();
        return packet.getResponseString();
    }

    @ProcessCommandPacket(MessagePacket.class)
    public String processData(MessagePacket packet) throws PacketProcessException {
        this.getMessage().message = packet.message;
        return packet.getResponseString();
    }

    @ProcessCommandPacket(SenderPacket.class)
    public String processSender(SenderPacket packet) throws PacketProcessException {
        this.getMessage().sender = packet.sender;
        return packet.getResponseString();
    }

    @ProcessCommandPacket(ReceiverPacket.class)
    public String processRecipients(ReceiverPacket packet) throws PacketProcessException {
        this.getMessage().recipients = packet.recipients;
        return packet.getResponseString();
    }

    @ProcessCommandPacket(SubjectPacket.class)
    public String processSubject(SubjectPacket packet) throws PacketProcessException {
        this.getMessage().subject = packet.subject;
        return packet.getResponseString();
    }

    @ProcessCommandPacket(SendPacket.class)
    public String processSend(SendPacket packet) throws PacketProcessException {
        var message = this.getMessage();
        if(message.sender == null) throw new PacketProcessException("no sender");
        if(message.subject == null) throw new PacketProcessException("no subject");
        if(message.message == null) throw new PacketProcessException("no data");
        if(message.recipients == null) throw new PacketProcessException("no recipients");
        this.message = null;
        return packet.getResponseString();
    }

    @ProcessCommandPacket(QuitPacket.class)
    public String processQuit(QuitPacket packet) {
        // quit
        return packet.getResponseString();
    }
}
