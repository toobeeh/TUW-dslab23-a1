package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.exceptions.PacketParseException;
import dslab.data.annotations.CommandPacket;
import dslab.data.exceptions.PacketProtocolException;

@CommandPacket("subject")
public class SubjectPacket implements Packet<SubjectPacket> {

    public String subject;

    @Override
    public SubjectPacket parseString(String data) throws PacketParseException, PacketProtocolException {
        var tokens = data.split(" ");
        if(!tokens[0].equals("subject")) throw new PacketProtocolException();
        if(tokens.length < 2) throw new PacketParseException("no subject");

        this.subject = data.substring(8);
        return this;
    }

    @Override
    public String toPacketString() {
        return "subject " + this.subject;
    }
}
