package dslab.data.dmap;

import dslab.data.Packet;
import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketParseException;
import dslab.util.tcp.exceptions.PacketProtocolException;

@CommandPacket("show")
public class ShowPacket implements Packet<ShowPacket> {

    public String messageId;

    @Override
    public ShowPacket parseString(String data) throws PacketParseException, PacketProtocolException {
        var tokens = data.split(" ");
        if(!tokens[0].equals("show")) throw new PacketProtocolException();
        if(tokens.length == 1) throw new PacketParseException("no message id");
        if(tokens.length > 2) throw new PacketParseException("too many arguments");

        this.messageId = tokens[1];
        return this;
    }

    @Override
    public String toPacketString() {
        return "show " + this.messageId;
    }

    @Override
    public Packet getResponsePacket() {
        return null; // implemented dynamically in protocol model
    }
}
