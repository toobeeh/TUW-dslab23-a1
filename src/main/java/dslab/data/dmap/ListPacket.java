package dslab.data.dmap;

import dslab.data.Packet;
import dslab.data.annotations.CommandPacket;
import dslab.data.exceptions.PacketProtocolException;

@CommandPacket("list")
public class ListPacket implements Packet<ListPacket> {
    public ListPacket parseString(String data) throws PacketProtocolException {
        if(!data.equals("list")) throw new PacketProtocolException();
        return this;
    }

    @Override
    public String toPacketString() {
        return "list";
    }

    @Override
    public Packet getResponsePacket() {
        return null; // implemented dynamically in protocol model
    }
}
