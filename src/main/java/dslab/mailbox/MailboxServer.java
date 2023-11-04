package dslab.mailbox;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.tcp.dmap.DMAPServerModel;
import dslab.util.tcp.ProtocolServerThread;
import dslab.util.Config;
import dslab.util.tcp.dmtp.DMTPServerModel;

public class MailboxServer implements IMailboxServer, Runnable {
    private final ProtocolServerThread dmtpServer;
    private final ProtocolServerThread dmapServer;
    private final MailStore mailStore;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final String serverName;
    private final Shell shell;

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
        var dmapPort = config.getInt("dmap.tcp.port");
        serverName = config.getString("domain");

        // init mail data store (data handling abstraction)
        var userCredentials = new HashMap<String,String>();
        var userConfig = new Config(config.getString("users.config"));
        for(var user : userConfig.listKeys()){
            userCredentials.put(user, userConfig.getString(user));
        }
        mailStore = new MailStore(userCredentials);

        // init servers
        dmtpServer = new ProtocolServerThread(dmtpPort, threadPool, this::createDmtpModel);
        dmapServer = new ProtocolServerThread(dmapPort, threadPool, this::createDmapModel);

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
        dmtp.setOnMessageSent(mailStore::storeMessage);
        dmtp.setRecipientValidator(recipients -> {
            DMTPServerModel.ValidatedRecipients result = new DMTPServerModel.ValidatedRecipients();
            for(var recipient : recipients){
                var tokens = recipient.split("@");
                if(!tokens[1].equals(serverName)) result.ignoredRecipients.add(recipient);
                else if(!mailStore.hasUser(tokens[0])) result.erroredRecipients.add(recipient);
                else result.validRecipients.add(recipient);
            }
            return result;
        });
        return dmtp;
    }
    /**
     * creates a new DMAP server model for client interaction
     * @return a fresh dmap protocol model
     */
    private DMAPServerModel createDmapModel(){
        var dmap = new DMAPServerModel();
        dmap.setCredentialsValidator(mailStore::validateCredentials);
        dmap.setMessageSupplier(mailStore::getUserMessages);
        return dmap;
    }

    @Override
    public void run() {
        dmtpServer.start();// dmtp server is a thread
        dmapServer.start();// dmap server is a thread
        shell.run(); // start shell in main thread
    }

    @Override
    @Command
    public void shutdown() {
        dmtpServer.shutdown();
        dmapServer.shutdown();
        threadPool.shutdown();
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
