package dslab.data;

import dslab.util.tcp.exceptions.PacketParseException;
import dslab.util.tcp.exceptions.PacketProtocolException;

public interface Packet<TPacket extends Packet> {

    TPacket parseString(String data) throws PacketParseException, PacketProtocolException;

    String toPacketString();

    default Packet getResponsePacket() { return new OkPacket(); }

}
