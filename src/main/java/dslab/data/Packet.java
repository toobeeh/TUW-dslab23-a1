package dslab.data;

import dslab.data.exceptions.PacketParseException;

public interface Packet<TPacket extends Packet> {

    TPacket parseString(String data) throws PacketParseException;

    public String toPacketString();

    default public String getResponseString() { return "ok"; }

}
