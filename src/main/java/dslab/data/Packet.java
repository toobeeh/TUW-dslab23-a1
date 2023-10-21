package dslab.data;

public interface Packet<TPacket extends Packet> {

    TPacket parseString(String data);

    public String toPacketString();

    public String getResponseString(boolean error);

}
