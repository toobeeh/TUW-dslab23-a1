package dslab.data;

import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketProtocolException;

@CommandPacket("quit")
public class QuitPacket implements Packet<QuitPacket> {
    public QuitPacket parseString(String data) throws PacketProtocolException {
        if(!data.equals("quit")) throw new PacketProtocolException();
        return new QuitPacket();
    }

    @Override
    public String toPacketString() {
        return "quit";
    }

    @Override
    public Packet getResponsePacket() {
        var ok = new OkPacket();
        ok.message = "bye";
        return ok;
    }
}
