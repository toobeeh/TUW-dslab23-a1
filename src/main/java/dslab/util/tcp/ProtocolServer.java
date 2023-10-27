package dslab.util.tcp;

import dslab.data.Packet;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * A thread that serves a protocol implementation to clients
 * The main thread listens for client connection; each client
 * gets a new thread from the provided executor service
 * with a new protocol interaction model created by the provided
 * factory.
 */
public class ProtocolServer extends Thread {
    private final TCPServer server;
    private final ConcurrentLinkedQueue<TCPClient> clients = new ConcurrentLinkedQueue<>();
    private final int port;
    private final Supplier<PacketProtocol> protocolSupplier;

    public ProtocolServer(int port, ExecutorService executor, Supplier<PacketProtocol> packetProtocolSupplier){
        this.port = port;
        this.server = new TCPServer(port, this::createClientWorker, executor);
        this.protocolSupplier = packetProtocolSupplier;
    }

    /**
     * Starts the TCP server and therefore listening for client connections
     */
    @Override
    public void run() {
        var name = "";
        {
            var p = protocolSupplier.get();
            name = p.protocolName();
        }

        // start the server with threadpool in a new thread
        this.setName("Protocol server " + name);
        this.server.run(); // in this thread
        System.out.println(name + " Server online on port " + port);
    }

    /**
     * Stops all currently connected clients and shuts down the
     * tcp server so that no new clients can connect
     */
    public void shutdown() {
        this.server.shutdown();
        for(var client: clients){
            client.shutdown();
        }
    }


    /**
     * initializes a client with a protocol interaction model
     * @param clientSocket the socket connection to the client
     * @return a runnable tcp protocol connection to the client
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
            Packet result;

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
