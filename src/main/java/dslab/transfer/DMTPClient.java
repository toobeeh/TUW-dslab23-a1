package dslab.transfer;

import dslab.data.annotations.CommandPacketHandler;
import dslab.data.dmtp.*;
import dslab.data.exceptions.PacketHandleException;
import dslab.data.exceptions.PacketProtocolException;
import dslab.util.tcp.PacketProtocol;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A class that models the flow of a DMTP connection on client-side
 * Does nothing but register accepted packets and check if fist Ok packet validates protocol
 */
public class DMTPClient extends PacketProtocol {
    public boolean isValidated() {
        return validated;
    }

    private boolean validated = false;

    @CommandPacketHandler
    public void handleOk(OkPacket packet) throws ProtocolCloseException {
        if(! validated) {
            validated = packet.message.equals("DMTP");
            if(!validated) throw new ProtocolCloseException(new ErrorPacket().withMessage("protocol error"));
        }
    }

    @CommandPacketHandler
    public void handleError(ErrorPacket error) {
        // oh no
    }

    @Override
    protected boolean protocolErrorIsFatal() {
        return false;
    }
}
