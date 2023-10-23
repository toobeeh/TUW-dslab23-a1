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
import dslab.util.tcp.dmtp.DMTPServer;

public class TransferServer implements ITransferServer, Runnable {

    private Shell shell;
    private DMTPServer dmtpServer;
    private MessageDispatcher messageDispatcher;
    private Thread dispatcherThread;
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

        messageDispatcher = new MessageDispatcher("transfer.one", port, monitoringHost, monitoringPort, threadPool);
        dispatcherThread = new Thread(messageDispatcher, "Message Dispatcher");
        dmtpServer = new DMTPServer(port, threadPool);

        dmtpServer.onMessageReceived = message -> messageDispatcher.queueMessage(message);

        shell = new Shell(in, out);
        shell.setPrompt(componentId + "> ");
        shell.register(this);
    }

    @Override
    public void run() {
        dispatcherThread.start();
        dmtpServer.run();
        shell.run();
    }

    @Override
    @Command
    public void shutdown() {
        dispatcherThread.stop();
        dmtpServer.shutdown();
        threadPool.shutdown();
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }

}
