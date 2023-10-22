package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.exceptions.PacketParseException;
import dslab.data.annotations.CommandPacket;
import dslab.data.exceptions.PacketProtocolException;

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
