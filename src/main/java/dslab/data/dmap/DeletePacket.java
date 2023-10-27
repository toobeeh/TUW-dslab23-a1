package dslab.data.dmap;

import dslab.data.Packet;
import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketParseException;
import dslab.util.tcp.exceptions.PacketProtocolException;

@CommandPacket("delete")
public class DeletePacket implements Packet<DeletePacket> {

    public String messageId;

    @Override
    public DeletePacket parseString(String data) throws PacketParseException, PacketProtocolException {
        var tokens = data.split(" ");
        if(!tokens[0].equals("delete")) throw new PacketProtocolException();
        if(tokens.length == 1) throw new PacketParseException("no message id");
        if(tokens.length > 2) throw new PacketParseException("too many arguments");

        this.messageId = tokens[1];
        return this;
    }

    @Override
    public String toPacketString() {
        return "delete " + this.messageId;
    }
}
