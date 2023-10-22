package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.exceptions.PacketParseException;
import dslab.data.annotations.CommandPacket;

@CommandPacket("send")
public class SendPacket implements Packet<SendPacket> {
    public SendPacket parseString(String data) throws PacketParseException {
        if(!data.equals("send")) throw new PacketParseException();
        return this;
    }

    @Override
    public String toPacketString() {
        return "send";
    }

}
