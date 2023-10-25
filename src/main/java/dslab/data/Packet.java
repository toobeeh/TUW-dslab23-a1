package dslab.data;

import dslab.data.exceptions.PacketParseException;
import dslab.data.exceptions.PacketProtocolException;

public interface Packet<TPacket extends Packet> {

    TPacket parseString(String data) throws PacketParseException, PacketProtocolException;

    String toPacketString();

    default Packet getResponsePacket() { return new OkPacket(); }

}
