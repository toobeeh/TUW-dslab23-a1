package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.PacketParseException;

import java.util.Arrays;
import java.util.List;

public class SenderPacket implements Packet<SenderPacket> {

    public String sender;
    public SenderPacket parseString(String data){
        var tokens = data.split(" ");
        if(!(tokens.length == 2) || !tokens[0].equals("from")) throw new PacketParseException();

        this.sender = tokens[1];
        return this;
    }

    @Override
    public String toPacketString() {
        return "from " + this.sender;
    }

    @Override
    public String getResponseString(boolean error) {
        return "ok";
    }
}
