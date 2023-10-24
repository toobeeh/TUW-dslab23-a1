package dslab.data.dmap;

import dslab.data.Packet;
import dslab.data.annotations.CommandPacket;
import dslab.data.exceptions.PacketParseException;
import dslab.data.exceptions.PacketProtocolException;

@CommandPacket("login")
public class LoginPacket implements Packet<LoginPacket> {

    public String password;
    public String username;

    @Override
    public LoginPacket parseString(String data) throws PacketParseException, PacketProtocolException {
        var tokens = data.split(" ");
        if(!tokens[0].equals("login")) throw new PacketProtocolException();
        if(tokens.length == 1) throw new PacketParseException("no credentials");
        if(tokens.length == 2) throw new PacketParseException("no password");
        if(tokens.length > 3) throw new PacketParseException("too many arguments");

        this.password = tokens[2];
        this.username = tokens[1];
        return this;
    }

    @Override
    public String toPacketString() {
        return "login " + this.username + " " + this.password;
    }
}
