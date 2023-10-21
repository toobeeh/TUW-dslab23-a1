package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.PacketFactory;
import dslab.data.PacketParseException;
import dslab.data.annotations.CommandPacketFactory;
import dslab.data.annotations.CommandPacketId;

@CommandPacketId("quit")
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

    @CommandPacketFactory
    public class QuitPacketFactory implements PacketFactory<QuitPacket> {

        @Override
        public QuitPacket create(String data) {
            return new QuitPacket();
        }
    }
}
