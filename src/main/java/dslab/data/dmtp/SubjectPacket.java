package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.PacketFactory;
import dslab.data.PacketParseException;
import dslab.data.annotations.CommandPacketFactory;
import dslab.data.annotations.CommandPacketId;

@CommandPacketId("subject")
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

    @CommandPacketFactory
    public class SubjectPacketFactory implements PacketFactory<SubjectPacket> {

        @Override
        public SubjectPacket create(String data) {
            return new SubjectPacket().parseString(data);
        }
    }
}
