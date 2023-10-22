package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.exceptions.PacketParseException;
import dslab.data.annotations.CommandPacket;

@CommandPacket("quit")
public class QuitPacket implements Packet<QuitPacket> {
    public QuitPacket parseString(String data) throws PacketParseException {
        if(!data.equals("quit")) throw new PacketParseException();
        return new QuitPacket();
    }

    @Override
    public String toPacketString() {
        return "quit";
    }

    @Override
    public String getResponseString() {
        return "ok bye";
    }
}
