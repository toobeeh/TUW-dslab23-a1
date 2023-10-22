package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.exceptions.PacketParseException;
import dslab.data.annotations.CommandPacket;

@CommandPacket("from")
public class SenderPacket implements Packet<SenderPacket> {

    public String sender;
    public SenderPacket parseString(String data) throws PacketParseException {
        var tokens = data.split(" ");
        if(!(tokens.length == 2) || !tokens[0].equals("from")) throw new PacketParseException();

        this.sender = tokens[1];
        return this;
    }

    @Override
    public String toPacketString() {
        return "from " + this.sender;
    }

}
