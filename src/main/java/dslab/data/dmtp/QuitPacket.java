package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.PacketParseException;

public class QuitPacket implements Packet<QuitPacket> {
    public QuitPacket parseString(String data){
        if(!data.equals("quit")) throw new PacketParseException();
        return new QuitPacket();
    }

    @Override
    public String toPacketString() {
        return "quit";
    }

    @Override
    public String getResponseString(boolean error) {
        return "ok bye";
    }
}
