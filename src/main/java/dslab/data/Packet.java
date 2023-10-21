package dslab.data;

public interface Packet<TPacket extends Packet> {

    TPacket parseString(String data) throws PacketParseException;

    public String toPacketString();

    default public String getResponseString() { return "ok"; }

}
