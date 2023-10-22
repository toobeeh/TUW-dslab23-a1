package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.exceptions.PacketParseException;
import dslab.data.annotations.CommandPacket;
import dslab.data.exceptions.PacketProtocolException;

@CommandPacket("send")
public class SendPacket implements Packet<SendPacket> {
    public SendPacket parseString(String data) throws PacketProtocolException {
        if(!data.equals("send")) throw new PacketProtocolException();
        return this;
    }

    @Override
    public String toPacketString() {
        return "send";
    }

}
