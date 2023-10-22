package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.exceptions.PacketParseException;
import dslab.data.annotations.CommandPacket;

@CommandPacket("begin")
public class BeginPacket implements Packet<BeginPacket> {
    public BeginPacket parseString(String data) throws PacketParseException {
        if(!data.equals("begin")) throw new PacketParseException();
        return this;
    }

    @Override
    public String toPacketString() {
        return "begin";
    }

}
