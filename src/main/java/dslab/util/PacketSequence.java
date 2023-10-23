package dslab.util;

import dslab.data.Packet;
import dslab.data.dmtp.*;
import dslab.util.tcp.PacketProtocol;
import dslab.util.tcp.dmtp.DMTPServerModel;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.util.ArrayList;
import java.util.List;

public class PacketSequence {

    List<Packet> packets;

    private Packet currentPacket;
    private Packet currentResult;

    public PacketSequence(Packet ...packets){
        this.packets = new ArrayList(List.of(packets));
    }

    public Packet next(){
        if(hasFinished()) return null;
        currentPacket = packets.remove(0);
        currentResult = null;
        return currentPacket;
    }

    public Packet checkResponse(String data, PacketProtocol protocol) throws ProtocolCloseException {

        // check if protocol can handle data
        try {
            currentResult = protocol.handle(data);
        } catch (ProtocolCloseException e) {
            currentResult = e.getResponsePacket();
            packets = List.of();
            throw e;
        }

        // response was not as expected; abort
        if(!data.equals(currentPacket.getResponsePacket().toPacketString())){
            packets = List.of();
        }

        return currentResult;
    }

    public boolean hasFinished(){
        return packets.size() == 0;
    }

    public static PacketSequence fromMessage(DMTPServerModel.Message message){

        var begin = new BeginPacket();
        var sender = new SenderPacket();
        sender.sender = message.sender;
        var subject = new SubjectPacket();
        subject.subject = message.subject;
        var recipient = new ReceiverPacket();
        recipient.recipients = message.recipients;
        var data = new MessagePacket();
        data.message = message.message;
        var send = new SendPacket();

        var sequence = new PacketSequence(begin, sender, subject, recipient, data, send);
        return sequence;
    }
}