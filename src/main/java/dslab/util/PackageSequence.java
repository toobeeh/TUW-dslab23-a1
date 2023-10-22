package dslab.util;

import dslab.data.Packet;
import dslab.util.tcp.PacketProtocol;
import dslab.util.tcp.TCPClient;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PackageSequence {

    private List<Packet> packets;

    public PackageSequence(Packet ...packets){
        this.packets = Arrays.asList(packets);
    }

    public void send(TCPClient client, PacketProtocol clientProtocol) throws ExecutionException, InterruptedException {
        for(var packet : packets){
            var nextComplete = new CompletableFuture<Packet>();
            var expectedPacket = packet.getResponsePacket();

            AtomicBoolean closed = new AtomicBoolean(false);
            client.onDataReceived = data -> {
                try {
                    var responsePacket = clientProtocol.handle(data);
                    nextComplete.complete(responsePacket);
                } catch (ProtocolCloseException e) {
                    nextComplete.complete(e.getResponsePacket());
                    closed.set(true);
                }
            };

            client.send(packet);
            var result = nextComplete.get();
            var errored = result.toPacketString().equals(expectedPacket.toPacketString());

            if(errored) {
                throw new RuntimeException("Unexpected packet: " + expectedPacket.toPacketString() + " <-> " + result.toPacketString());
            }
            if(closed.get()) break;
        }
    }
}
