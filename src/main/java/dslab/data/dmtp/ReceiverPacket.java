package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.exceptions.PacketParseException;
import dslab.data.annotations.CommandPacket;
import dslab.data.exceptions.PacketProtocolException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandPacket("to")
public class ReceiverPacket implements Packet<ReceiverPacket> {

    public List<String> recipients;
    public ReceiverPacket parseString(String data) throws PacketParseException, PacketProtocolException {
        var tokens = data.split(" ");
        if(!tokens[0].equals("to")) throw new PacketProtocolException();
        if(!(tokens.length == 2)) throw new PacketParseException("no recipients");

        this.recipients = Arrays.asList(tokens[1].split(","));
        return this;
    }

    @Override
    public String toPacketString() {
        return "to " + this.recipients.stream().collect(Collectors.joining(","));
    }


    @Override
    public Packet getResponsePacket() {
        var ok = new OkPacket();
        ok.message = recipients.size() + "";
        return ok;
    }
}
