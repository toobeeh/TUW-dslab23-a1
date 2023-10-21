package dslab.data.dmtp;

import dslab.data.*;
import dslab.data.annotations.CommandPacketFactory;
import dslab.data.annotations.CommandPacketId;

@CommandPacketId("data")
public class MessagePacket implements Packet<MessagePacket> {

    public String message;
    public MessagePacket parseString(String data){
        var tokens = data.split(" ");
        if(tokens.length < 2 || !tokens[0].equals("data")) throw new PacketParseException();

        this.message = data.substring(5);
        return this;
    }

    @Override
    public String toPacketString() {
        return "data " + this.message;
    }

    @Override
    public String getResponseString(boolean error) {
        return error ? "error no recipients" : "ok";
    }

    @CommandPacketFactory
    public static class MessagePacketFactory implements PacketFactory<MessagePacket> {
        @Override
        public MessagePacket create(String data) {
            return new MessagePacket().parseString(data);
        }
    }
}
