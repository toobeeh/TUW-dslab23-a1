package dslab.transfer;

import dslab.data.PacketProcessException;
import dslab.data.annotations.ProcessCommandPacket;
import dslab.data.dmtp.MessagePacket;
import dslab.data.dmtp.ReceiverPacket;
import dslab.data.dmtp.SubjectPacket;
import dslab.util.tcp.PacketProtocol;

import java.util.List;

class Message {
    String subject;
    String sender;
    List<String> recipients;
}

public class DMTP extends PacketProtocol {

    @ProcessCommandPacket(MessagePacket.class)
    public String processData(MessagePacket packet){
        return packet.getResponseString();
    }

    @ProcessCommandPacket(ReceiverPacket.class)
    public String processRecipients(ReceiverPacket packet){

        return packet.getResponseString();
    }

    @ProcessCommandPacket(SubjectPacket.class)
    public String processSubject(SubjectPacket packet) throws PacketProcessException {
        System.out.println(packet.subject);
        if(packet.subject.equals("hi")) throw new PacketProcessException("hello there");
        return packet.getResponseString();
    }
}
