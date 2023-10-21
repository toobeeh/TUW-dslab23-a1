package dslab.transfer;

import dslab.data.annotations.ProcessCommandPacket;
import dslab.data.dmtp.MessagePacket;
import dslab.data.dmtp.ReceiverPacket;
import dslab.data.dmtp.SubjectPacket;
import dslab.util.tcp.PacketParser;

public class DMTPParser extends PacketParser {

    @ProcessCommandPacket(MessagePacket.class)
    public String processData(MessagePacket packet){

        return packet.getResponseString(false);
    }

    @ProcessCommandPacket(ReceiverPacket.class)
    public String processRecipients(ReceiverPacket packet){

        return packet.getResponseString(false);
    }

    @ProcessCommandPacket(SubjectPacket.class)
    public String processSubject(SubjectPacket packet){
        System.out.println(packet.subject);
        return packet.getResponseString(false);
    }
}
