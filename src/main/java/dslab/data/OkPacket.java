package dslab.data;

import dslab.util.tcp.annotations.CommandPacket;
import dslab.util.tcp.exceptions.PacketProtocolException;

@CommandPacket("ok")
public class OkPacket implements Packet<OkPacket> {
    public String message;

    public OkPacket parseString(String data) throws PacketProtocolException {
        if(!data.startsWith("ok")) throw new PacketProtocolException();
        if(data.length() > 3) message = data.substring(3);
        return this;
    }

    public OkPacket withMessage(String message){
        this.message = message;
        return this;
    }

    @Override
    public String toPacketString() {
        return "ok" + (message == null ? "" : " " + message);
    }
}