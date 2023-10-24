package dslab.data.dmap;

import dslab.data.Packet;
import dslab.data.annotations.CommandPacket;
import dslab.data.exceptions.PacketProtocolException;

@CommandPacket("logout")
public class LogoutPacket implements Packet<LogoutPacket> {
    public LogoutPacket parseString(String data) throws PacketProtocolException {
        if(!data.equals("logout")) throw new PacketProtocolException();
        return this;
    }

    @Override
    public String toPacketString() {
        return "logout";
    }

}
