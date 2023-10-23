package dslab.mailbox;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import dslab.ComponentFactory;
import dslab.data.Packet;
import dslab.util.tcp.dmtp.DMTPServer;
import dslab.util.tcp.dmtp.DMTPServerModel;
import dslab.util.Config;
import dslab.util.tcp.TCPClient;
import dslab.util.tcp.TCPPooledServer;
import dslab.util.tcp.exceptions.ProtocolCloseException;

public class MailboxServer implements IMailboxServer, Runnable {
    private DMTPServer dmtpServer;
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out) {
        var dmtpPort = config.getInt("dmtp.tcp.port");

        dmtpServer = new DMTPServer(dmtpPort, threadPool);
        dmtpServer.onMessageReceived = message -> {
            System.out.println(message.sender + " -> " + String.join(",", message.recipients) + "\n" + message.subject + ":\n" + message.message + "\n---- \n");
        };
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
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
