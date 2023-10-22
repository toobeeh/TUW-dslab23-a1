package dslab.data;

import dslab.data.exceptions.PacketParseException;
import dslab.data.exceptions.PacketProtocolException;

public interface Packet<TPacket extends Packet> {

    TPacket parseString(String data) throws PacketParseException, PacketProtocolException;

    public String toPacketString();

    default public String getResponseString() { return "ok"; }

}
