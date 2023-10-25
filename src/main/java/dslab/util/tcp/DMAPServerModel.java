package dslab.util.tcp;

import dslab.data.AnonymousPacket;
import dslab.data.ErrorPacket;
import dslab.data.annotations.CommandPacketHandler;
import dslab.data.dmap.*;
import dslab.data.exceptions.PacketHandleException;
import dslab.util.Message;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DMAPServerModel extends PacketProtocol {

    public static class Credentials {
        public String login;
        public String password;
    }

    private Credentials credentials = null;
    private Integer loginAttempts = null;
    private Function<Credentials, Boolean> validateCredentials = null;
    public void setCredentialsValidator(Function<Credentials, Boolean> validateCredentials) {
        this.validateCredentials = validateCredentials;
    }

    private Credentials getCredentials() throws PacketHandleException {
        if(credentials == null) throw new PacketHandleException("not logged in");
        return credentials;
    }

    private Function<String, ConcurrentHashMap<String, Message>> messageSupplier = null;
    public void setMessageSupplier(Function<String, ConcurrentHashMap<String, Message>> messageSupplier) {
        this.messageSupplier = messageSupplier;
    }

    @CommandPacketHandler
    public void handleLogin(LoginPacket packet) throws PacketHandleException, ProtocolCloseException {
        if(validateCredentials == null) throw new PacketHandleException("server error");

        var credentials = new Credentials();
        credentials.login = packet.username;
        credentials.password = packet.password;
        var success = validateCredentials.apply(credentials);

        if(success) {
            loginAttempts = null;
            this.credentials = credentials;
        }
        else {
            loginAttempts = loginAttempts == null ? 1 : loginAttempts+1;
            if(loginAttempts >= 3) throw new ProtocolCloseException(new ErrorPacket().withMessage("too many login attempts"));
            else throw new PacketHandleException("invalid credentials");
        }
    }

    @CommandPacketHandler
    public void handleLogout(LogoutPacket packet) throws PacketHandleException {
        if(credentials == null) {
            throw new PacketHandleException("not logged in");
        }
        credentials = null;
    }

    @CommandPacketHandler
    public AnonymousPacket handleList(ListPacket packet) throws PacketHandleException {
        var c = getCredentials();
        var messages = messageSupplier.apply(c.login);
        var list = messages.entrySet().stream().map(entry -> {
            var message = entry.getValue();
            return entry.getKey() + " " + message.sender + " " + message.subject;
        }).collect(Collectors.joining("\n"));

        return new AnonymousPacket().parseString(list);
    }

    @CommandPacketHandler
    public AnonymousPacket handleShow(ShowPacket packet) throws PacketHandleException {
        var c = getCredentials();
        var messages = messageSupplier.apply(c.login);
        var message = messages.get(packet.messageId);
        if(message == null) throw new PacketHandleException("unknown message id");

        var response = "";
        response += "from " + message.sender + "\n";
        response += "to " + String.join(",",message.recipients) + "\n";
        response += "subject " + message.subject + "\n";
        response += "data " + message.message;

        return new AnonymousPacket().parseString(response);
    }

    @CommandPacketHandler
    public void handleDelete(DeletePacket packet) throws PacketHandleException {
        var c = getCredentials();
        var messages = messageSupplier.apply(c.login);
        var message = messages.get(packet.messageId);
        if(message == null) throw new PacketHandleException("unknown message id");
        messages.remove(packet.messageId);
    }

    @Override
    protected boolean protocolErrorIsFatal() {
        return true;
    }

    @Override
    public String protocolName() {
        return "DMAP";
    }

}
