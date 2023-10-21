package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.PacketFactory;
import dslab.data.PacketParseException;
import dslab.data.annotations.CommandPacketFactory;
import dslab.data.annotations.CommandPacketId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandPacketId("to")
@CommandPacketFactory(ReceiverPacket.ReceiverPacketFactory.class)
public class ReceiverPacket implements Packet<ReceiverPacket> {

    public List<String> recipients;
    public ReceiverPacket parseString(String data){
        var tokens = data.split(" ");
        if(!(tokens.length == 2) || !tokens[0].equals("to")) throw new PacketParseException();

        this.recipients = Arrays.asList(tokens[1].split(","));
        return this;
    }

    @Override
    public String toPacketString() {
        return "to " + this.recipients.stream().collect(Collectors.joining(","));
    }

    @Override
    public String getResponseString(boolean error) {
        return "ok " + this.recipients.size();
    }


    public static class ReceiverPacketFactory implements PacketFactory<MessagePacket> {
        @Override
        public MessagePacket create(String data) {
            return new MessagePacket().parseString(data);
        }
    }
}
