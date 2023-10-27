package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketProtocolException;

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
