package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.PacketFactory;
import dslab.data.PacketParseException;
import dslab.data.annotations.CommandPacketFactory;
import dslab.data.annotations.CommandPacketId;

@CommandPacketId("begin")
public class BeginPacket implements Packet<BeginPacket> {
    public BeginPacket parseString(String data){
        if(!data.equals("begin")) throw new PacketParseException();
        return this;
    }

    @Override
    public String toPacketString() {
        return "begin";
    }

    @Override
    public String getResponseString(boolean error) {
        return "ok";
    }

    @CommandPacketFactory
    public class BeginPacketFactory implements PacketFactory<BeginPacket> {

        @Override
        public BeginPacket create(String data) {
            return new BeginPacket().parseString(data);
        }
    }
}
