package dslab.transfer;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import dslab.ComponentFactory;
import dslab.data.monitoring.MonitoringPacket;
import dslab.mailbox.MessageDispatcher;
import dslab.util.Config;
import dslab.util.PacketSequence;
import dslab.util.dns.DNS;
import dslab.util.tcp.dmtp.DMTPClientModel;
import dslab.util.tcp.dmtp.DMTPServer;
import dslab.util.tcp.dmtp.DMTPServerModel;
import dslab.util.tcp.exceptions.ProtocolCloseException;
import dslab.util.udp.UDPSender;

public class TransferServer implements ITransferServer, Runnable {

    private DMTPServer dmtpServer;
    private MessageDispatcher messageDispatcher;
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out) {
        var port = config.getInt("tcp.port");
        var monitoringHost = config.getString("monitoring.host");
        var monitoringPort = config.getInt("monitoring.port");

        messageDispatcher = new MessageDispatcher("localhost", port, monitoringHost, monitoringPort, threadPool);
        dmtpServer = new DMTPServer(port, threadPool);

        dmtpServer.onMessageReceived = message -> messageDispatcher.handleMessage(message);
    }

    @Override
    public void run() {
        this.dmtpServer.run();
    }

    @Override
    public void shutdown() {
        this.dmtpServer.shutdown();
        this.threadPool.shutdown();
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }

}
