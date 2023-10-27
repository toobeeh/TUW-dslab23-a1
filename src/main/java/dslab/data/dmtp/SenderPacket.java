package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.util.tcp.exceptions.PacketParseException;
import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketProtocolException;
import dslab.util.Message;

@CommandPacket("from")
public class SenderPacket implements Packet<SenderPacket> {

    public String sender;
    public SenderPacket parseString(String data) throws PacketParseException, PacketProtocolException {
        var tokens = data.split(" ");
        if(!tokens[0].equals("from")) throw new PacketProtocolException();
        if(!(tokens.length == 2)) throw new PacketParseException("no sender");
        if(!Message.isValidEmail(tokens[1])) throw new PacketParseException("invalid address " + tokens[1]);

        this.sender = tokens[1];
        return this;
    }

    @Override
    public String toPacketString() {
        return "from " + this.sender;
    }

}
