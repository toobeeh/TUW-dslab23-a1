package dslab.transfer;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import dslab.ComponentFactory;
import dslab.data.Packet;
import dslab.data.dmtp.ErrorPacket;
import dslab.data.monitoring.MonitoringPacket;
import dslab.util.Config;
import dslab.util.dns.DNS;
import dslab.util.tcp.TCPClient;
import dslab.util.tcp.TCPPooledServer;
import dslab.util.tcp.exceptions.ProtocolCloseException;
import dslab.util.udp.UDPSender;

public class TransferServer implements ITransferServer, Runnable {

    private TCPPooledServer server;
    private UDPSender monitoringClient;
    private DNS dns = new DNS();
    private int port;
    private ConcurrentLinkedQueue<TCPClient> clients = new ConcurrentLinkedQueue<>();

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out) {

        // open connection to monitoring
        var monitoringHost = config.getString("monitoring.host");
        var monitoringPort = config.getInt("monitoring.port");
        try {
            this.monitoringClient = new UDPSender(monitoringHost, monitoringPort);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        // start the server on config port and provide a callback to create worker threads
        // the worker threads will handle the client socket connections
        // and get dispatched by the servers thread pool
        port = config.getInt("tcp.port");
        this.server = new TCPPooledServer(port, this::handleClient);
    }

    @Override
    public void run() {

        // start the server with threadpool in a new thread
        new Thread(this.server, "TCP Server Pool").start();
        System.out.println("TCP Server Pool has started");
    }

    @Override
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
    private TCPClient handleClient(Socket clientSocket){
        var client = new TCPClient(clientSocket);
        var protocol = new DMTPServer();

        // send message
        protocol.onMessageSent = message -> {
            reportMessageToMonitoring(message);
        };

        // init socket event handlers
        client.onSocketReady = () -> {
            System.out.println("DMTP Client connected");
            client.send("ok DMTP");
        };

        // init shutdown event
        client.onSocketShutdown = () -> System.out.println("Client disconnected");

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

            // log command & response
            System.out.println(String.format("%1$30s", data) + " >> " + result.toPacketString());
        };

        client.run();
        return client;
    }

    private void reportMessageToMonitoring(DMTPServer.Message message){
        MonitoringPacket monitoring = new MonitoringPacket();
        monitoring.port = port;
        monitoring.host = "test";
        monitoring.email = message.sender;
        this.monitoringClient.send(monitoring.toPacketString());
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }

}
