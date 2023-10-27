package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketProtocolException;

@CommandPacket("begin")
public class BeginPacket implements Packet<BeginPacket> {
    public BeginPacket parseString(String data) throws PacketProtocolException {
        if(!data.equals("begin")) throw new PacketProtocolException();
        return this;
    }

    @Override
    public String toPacketString() {
        return "begin";
    }

}
