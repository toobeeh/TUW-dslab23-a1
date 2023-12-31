package dslab.data.dmtp;

import dslab.data.OkPacket;
import dslab.data.Packet;
import dslab.util.tcp.exceptions.PacketParseException;
import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketProtocolException;
import dslab.util.Message;

import java.util.Arrays;
import java.util.List;

@CommandPacket("to")
public class ReceiverPacket implements Packet<ReceiverPacket> {

    public List<String> recipients;
    public ReceiverPacket parseString(String data) throws PacketParseException, PacketProtocolException {
        var tokens = data.split(" ");
        if(!tokens[0].equals("to")) throw new PacketProtocolException();
        if(!(tokens.length == 2)) throw new PacketParseException("no recipients");

        this.recipients = Arrays.asList(tokens[1].split(","));
        for(var recipient : recipients){
            if(!Message.isValidEmail(recipient)) throw new PacketParseException("invalid address " + recipient);
        }
        return this;
    }

    @Override
    public String toPacketString() {
        return "to " + String.join(",",this.recipients);
    }


    @Override
    public Packet getResponsePacket() {
        var ok = new OkPacket();
        ok.message = recipients.size() + "";
        return ok;
    }
}
