package dslab.data.dmtp;

import dslab.data.*;
import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketParseException;
import dslab.util.tcp.exceptions.PacketProtocolException;

@CommandPacket("data")
public class MessagePacket implements Packet<MessagePacket> {

    public String message;
    public MessagePacket parseString(String data) throws PacketParseException, PacketProtocolException {
        if(!data.startsWith("data")) throw new PacketProtocolException();
        if(data.split(" ").length < 2) throw new PacketParseException("no message");

        this.message = data.substring(5).replace("<br>", "\n");
        return this;
    }

    @Override
    public String toPacketString() {
        return "data " + this.message.replace("\n", "<br>");
    }

}
