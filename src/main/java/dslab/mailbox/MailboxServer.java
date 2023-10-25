package dslab.mailbox;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Message;
import dslab.util.tcp.dmap.DMAPServerModel;
import dslab.util.tcp.ProtocolServer;
import dslab.util.Config;
import dslab.util.tcp.dmtp.DMTPServerModel;

public class MailboxServer implements IMailboxServer, Runnable {
    private final ProtocolServer dmtpServer;
    private final ProtocolServer dmapServer;
    private final MailStore mailStore;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final String serverName;

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

        // init mail data store
        var userCredentials = new HashMap<String,String>();
        var userConfig = new Config(config.getString("users.config"));
        for(var user : userConfig.listKeys()){
            userCredentials.put(user, userConfig.getString(user));
        }
        mailStore = new MailStore(userCredentials);

        // init servers
        dmtpServer = new ProtocolServer(dmtpPort, threadPool, this::createDmtpModel);
        dmapServer = new ProtocolServer(dmapPort, threadPool, this::createDmapModel);

        var shell = new Shell(in, out);
        shell.setPrompt(componentId + "> ");
        shell.register(this);
    }

    private DMTPServerModel createDmtpModel(){
        var dmtp = new DMTPServerModel();
        dmtp.setOnMessageSent(mailStore::storeMessage);
        dmtp.setRecipientValidator(recipients -> {
            var faultyUsers = recipients.stream().map(user -> {
                    var tokens = user.split("@");
                    if(!tokens[1].equals(serverName)) return null;
                    if(!mailStore.hasUser(tokens[0])) return null;
                    return user;
            }).filter(user -> user != null).collect(Collectors.toList());
            return faultyUsers;
        });
        return dmtp;
    }

    private DMAPServerModel createDmapModel(){
        var dmap = new DMAPServerModel();
        dmap.setCredentialsValidator(mailStore::validateCredentials);
        dmap.setMessageSupplier(mailStore::getUserMessages);
        return dmap;
    }

    @Override
    public void run() {
        dmtpServer.run();
        dmapServer.run();
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
