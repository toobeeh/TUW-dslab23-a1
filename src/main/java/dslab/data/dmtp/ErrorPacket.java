package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.annotations.CommandPacket;
import dslab.data.exceptions.PacketParseException;
import dslab.data.exceptions.PacketProtocolException;

@CommandPacket("error")
public class ErrorPacket implements Packet<ErrorPacket> {
    private String message;
    public ErrorPacket parseString(String data) throws PacketProtocolException, PacketParseException {
        if(!data.equals("error")) throw new PacketProtocolException();
        if(data.length() <= 6 ) throw new PacketParseException("no message");
        message = data.substring(7);
        return this;
    }

    public ErrorPacket withMessage(String message){
        this.message = message;
        return this;
    }

    @Override
    public String toPacketString() {
        return "error " + message;
    }

}
