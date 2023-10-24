package dslab.mailbox;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dslab.ComponentFactory;
import dslab.util.Message;
import dslab.util.tcp.DMAPServerModel;
import dslab.util.tcp.ProtocolServer;
import dslab.util.Config;
import dslab.util.tcp.dmtp.DMTPServerModel;

public class MailboxServer implements IMailboxServer, Runnable {
    private ProtocolServer dmtpServer, dmapServer;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Message>> storedMessages = new ConcurrentHashMap<>();
    private final HashMap<String, String> userCredentials; // need not be thread safe since readonly
    private final String serverName;
    private int nextMessageId = 0;

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

        // load users
        userCredentials = new HashMap<String,String>();
        var userConfig = new Config(config.getString("users.config"));
        for(var user : userConfig.listKeys()){
            userCredentials.put(user, userConfig.getString(user));
        }

        // init servers
        dmtpServer = new ProtocolServer(dmtpPort, threadPool, this::createDmtpModel);
        dmapServer = new ProtocolServer(dmapPort, threadPool, this::createDmapModel);
    }

    private DMTPServerModel createDmtpModel(){
        var dmtp = new DMTPServerModel();
        dmtp.setOnMessageSent(message -> {
            System.out.println(message.sender + " -> " + String.join(",", message.recipients) + "\n" + message.subject + ":\n" + message.message + "\n---- \n");
            storeMessage(message);
        });
        dmtp.setRecipientValidator(recipients -> {
            var faultyUsers = recipients.stream().map(user -> {
                    var tokens = user.split("@");
                    if(!tokens[1].equals(serverName)) return null;
                    if(userCredentials.get(tokens[0]) != null) return null;
                    return user;
            }).filter(user -> user != null).collect(Collectors.toList());
            return faultyUsers;
        });
        return dmtp;
    }

    private void storeMessage(Message message){
        for(var user : message.recipients){
            var username = user.split("@")[0];
            var userMap = getUserMessages(username);
            var id = getNextId().toString();
            userMap.put(id, message);
        }
    }

    private synchronized Integer getNextId(){
        return nextMessageId++;
    }

    private boolean validateCredentials(DMAPServerModel.Credentials credentials){
        return credentials.password.equals(userCredentials.get(credentials.login));
    }

    private ConcurrentHashMap<String, Message> getUserMessages(String username){
        var userMap = storedMessages.get(username);
        if(userMap != null) return userMap;

        // it is more efficient to synchronize only this block (this will only hit the first time a user connects)
        // and re-fetch the list, than to synchronize the whole method
        synchronized (this.storedMessages) {
            var refetch = storedMessages.get(username);
            if(refetch != null) return refetch;

            userMap = new ConcurrentHashMap<>();
            storedMessages.put(username, userMap);
        }
        return userMap;
    }

    private DMAPServerModel createDmapModel(){
        var dmap = new DMAPServerModel();
        dmap.setCredentialsValidator(this::validateCredentials);
        dmap.setMessageSupplier(this::getUserMessages);
        return dmap;
    }

    @Override
    public void run() {
        dmtpServer.run();
        dmapServer.run();
    }

    @Override
    public void shutdown() {
        dmtpServer.shutdown();
        dmapServer.shutdown();
        threadPool.shutdown();
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
