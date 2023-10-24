package dslab.util.tcp;

import dslab.data.Packet;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class ProtocolServer implements Runnable {
    private TCPPooledServer server;
    private ConcurrentLinkedQueue<TCPClient> clients = new ConcurrentLinkedQueue<>();
    private int port;
    private Supplier<PacketProtocol> protocolSupplier;

    public ProtocolServer(int port, ExecutorService executor, Supplier<PacketProtocol> packetProtocolSupplier){
        this.port = port;
        this.server = new TCPPooledServer(port, this::createClientWorker, executor);
        this.protocolSupplier = packetProtocolSupplier;
    }

    @Override
    public void run() {

        var name = "";
        {
            var p = protocolSupplier.get();
            name = p.protocolName();
        }

        // start the server with threadpool in a new thread
        new Thread(this.server, name + " Server").start();
        System.out.println(name + " Server online on port " + port);
    }

    public void shutdown() {
        for(var client: clients){
            client.shutdown();
        }
        this.server.shutdown();
    }


    /**
     * initializes a DMTP client
     * @param clientSocket the socket connection to the client
     * @return a runnable dmtp connection to the client
     */
    private TCPClient createClientWorker(Socket clientSocket){
        var client = new TCPClient(clientSocket);
        var protocol = protocolSupplier.get();

        // init socket event handlers and greet with protocol name
        client.getOnSocketReady().thenAccept(readyClient -> {
            client.send("ok " + protocol.protocolName());
        });

        // init shutdown event
        client.getOnSocketShutdown().thenAccept(shutdownClient -> {
            clients.remove(shutdownClient); // this is called in the socket thread -> clients list needs to be thread-safe
        });

        // init command event
        client.onDataReceived = (data) -> {
            Packet result = null;

            // handle incoming protocol command
            try {
                result = protocol.handle(data);
                client.send(result);
            } catch (ProtocolCloseException e) {

                // protocol handler decided to end the connection
                result = e.getResponsePacket();
                client.send(result);
                client.shutdown();
            }
        };

        clients.add(client);
        return client;
    }
}
