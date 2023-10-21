package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.PacketParseException;
import dslab.data.annotations.CommandPacketId;

@CommandPacketId("subject")
public class SubjectPacket implements Packet<SubjectPacket> {

    public String subject;

    @Override
    public SubjectPacket parseString(String data) throws PacketParseException {
        var tokens = data.split(" ");
        if(tokens.length < 2 || !tokens[0].equals("subject")) throw new PacketParseException("no subject");

        this.subject = data.substring(8);
        return this;
    }

    @Override
    public String toPacketString() {
        return "subject " + this.subject;
    }

    @Override
    public String getResponseString() {
        return "ok";
    }
}
