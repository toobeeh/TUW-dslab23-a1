package dslab.util.tcp.dmtp;

import dslab.data.Packet;
import dslab.util.dns.DNS;
import dslab.util.dns.DomainNameNotFoundException;
import dslab.util.tcp.TCPClient;
import dslab.util.tcp.TCPPooledServer;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class DMTPServer implements Runnable {
    private TCPPooledServer server;
    private ConcurrentLinkedQueue<TCPClient> clients = new ConcurrentLinkedQueue<>();
    private int port;
    public Consumer<DMTPServerModel.Message> onMessageReceived;

    public DMTPServer(int port, ExecutorService executor){
        this.port = port;
        this.server = new TCPPooledServer(port, this::createClientWorker, executor);
    }

    @Override
    public void run() {

        // start the server with threadpool in a new thread
        new Thread(this.server, "DMTP Server Pool").start();
        System.out.println("DMTP Server Pool online on port " + port);
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
        var protocol = new DMTPServerModel();

        // send message
        protocol.onMessageSent = message -> {
            if(onMessageReceived != null) onMessageReceived.accept(message);
        };

        // init socket event handlers
        client.getOnSocketReady().thenAccept(readyClient -> {
            client.send("ok DMTP");
        });

        // init shutdown event
        client.getOnSocketShutdown().thenAccept(shutdownClient -> {
            clients.remove(shutdownClient); // this is called in the socket thread -> clients list needs to be thread-safe
        });

        // init command event
        client.onDataReceived = (data) -> {
            Packet result = null;

            // handle incoming DMTP command
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