package dslab.data.dmtp;

import dslab.data.Packet;
import dslab.data.annotations.CommandPacket;
import dslab.data.exceptions.PacketParseException;
import dslab.data.exceptions.PacketProtocolException;

@CommandPacket("ok")
public class OkPacket implements Packet<OkPacket> {
    public String message;

    public OkPacket parseString(String data) throws PacketProtocolException {
        if(!data.equals("ok")) throw new PacketProtocolException();
        if(data.length() > 3) message = data.substring(4);
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