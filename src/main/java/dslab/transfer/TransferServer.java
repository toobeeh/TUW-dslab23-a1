package dslab.transfer;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;
import dslab.util.tcp.ProtocolServerThread;
import dslab.util.tcp.dmtp.DMTPServerModel;

public class TransferServer implements ITransferServer, Runnable {

    private final Shell shell;
    private final ProtocolServerThread dmtpServer;
    private final MessageDispatcherThread messageDispatcher;
    private final ExecutorService clientThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService messageThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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

        messageDispatcher = new MessageDispatcherThread(port, monitoringHost, monitoringPort, messageThreadPool);
        dmtpServer = new ProtocolServerThread(port, clientThreadPool, this::createDmtpModel);

        shell = new Shell(in, out);
        shell.setPrompt(componentId + "> ");
        shell.register(this);
    }

    /**
     * creates a new DMTP server model for client interaction
     * @return a fresh dmtp protocol model
     */
    private DMTPServerModel createDmtpModel(){
        var dmtp = new DMTPServerModel();
        dmtp.setOnMessageSent(message -> messageDispatcher.queueMessage(message));
        return dmtp;
    }

    @Override
    public void run() {
        messageDispatcher.start(); // message dispatcher is a thread
        dmtpServer.start(); // dmtp server is a thread
        shell.run(); // start shell in main thread
    }

    @Override
    @Command
    public void shutdown() {
        messageDispatcher.shutdown();
        dmtpServer.shutdown();
        clientThreadPool.shutdown();
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }

}
