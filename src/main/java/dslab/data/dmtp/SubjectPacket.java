package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.PacketParseException;

public class SubjectPacket implements Packet<SubjectPacket> {

    public String subject;

    @Override
    public SubjectPacket parseString(String data){
        var tokens = data.split(" ");
        if(tokens.length < 2 || !tokens[0].equals("subject")) throw new PacketParseException();

        this.subject = data.substring(8);
        return this;
    }

    @Override
    public String toPacketString() {
        return "subject " + this.subject;
    }

    @Override
    public String getResponseString(boolean error) {
        return "ok";
    }
}
