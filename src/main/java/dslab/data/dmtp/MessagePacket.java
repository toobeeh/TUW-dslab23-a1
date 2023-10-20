package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.PacketParseException;

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
        return error ? "error no recipients" : ok";
    }
}
