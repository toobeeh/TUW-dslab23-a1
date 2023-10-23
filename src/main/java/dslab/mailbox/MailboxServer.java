package dslab.mailbox;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

import dslab.ComponentFactory;
import dslab.data.Packet;
import dslab.transfer.DMTPServer;
import dslab.util.Config;
import dslab.util.tcp.TCPClient;
import dslab.util.tcp.TCPPooledServer;
import dslab.util.tcp.exceptions.ProtocolCloseException;

public class MailboxServer implements IMailboxServer, Runnable {

    private TCPPooledServer dmapServer, dmtpServer;
    private int dmtpPort, dmapPort;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out) {


        // start the dmap & dmtp server on config port and provide a callback to create worker threads
        // the worker threads will handle the client socket connections
        // and get dispatched by the servers thread pool
        dmtpPort = config.getInt("dmtp.tcp.port");
        this.dmtpServer = new TCPPooledServer(dmtpPort, this::handleDmtpClient);

        dmapPort = config.getInt("dmap.tcp.port");
        this.dmapServer = new TCPPooledServer(dmapPort, this::handleDmtpClient);
    }

    private TCPClient handleDmapClient(Socket clientSocket){
        var client = new TCPClient(clientSocket);

        // init socket event handlers
        client.getOnSocketReady().thenAccept(readyClient -> {
            System.out.println("DMAP Client connected");
            client.send("ok DMAP");
        });

        // init shutdown event
        client.getOnSocketShutdown().thenAccept(shutdownCLient -> System.out.println("DMAP Client disconnected"));

        // init command event
        client.onDataReceived = (data) -> { };

        client.run();
        return client;
    }

    /**
     * initializes a DMTP client
     * @param clientSocket the socket connection to the client
     * @return a runnable dmtp connection to the client
     */
    private TCPClient handleDmtpClient(Socket clientSocket){
        var client = new TCPClient(clientSocket);
        var protocol = new DMTPServer();

        // send message
        protocol.onMessageSent = message -> {
            System.out.println(message);
        };

        // init socket event handlers
        client.getOnSocketReady().thenAccept(readyClient -> {
            client.send("ok DMTP");
        });

        // init shutdown event
        client.getOnSocketShutdown().thenAccept(shutdownCLient -> System.out.println("DMTP Client disconnected"));

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
            //System.out.println(String.format("%1$30s", data) + " >> " + result.toPacketString());
        };

        client.run();
        return client;
    }

    @Override
    public void run() {
        // start the servers with threadpool in a new thread
        new Thread(this.dmtpServer, "TCP DMTP Server Pool").start();
        System.out.println("TCP DMTP Server Pool has started on :" + dmtpPort);

        new Thread(this.dmapServer, "TCP DMAP Server Pool").start();
        System.out.println("TCP DMAP Server Pool has started on :" + dmapPort);
    }

    @Override
    public void shutdown() {
        // TODO
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
