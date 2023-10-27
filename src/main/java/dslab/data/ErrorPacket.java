package dslab.data;

import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketProtocolException;

@CommandPacket("error")
public class ErrorPacket implements Packet<ErrorPacket> {
    public String message;
    public ErrorPacket parseString(String data) throws PacketProtocolException {
        if(!data.startsWith("error")) throw new PacketProtocolException();
        message = data.length() > 6 ? data.substring(6) : null;
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
