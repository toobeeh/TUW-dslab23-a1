package dslab.mailbox;

import dslab.util.Message;
import dslab.util.tcp.dmap.DMAPServerModel;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class MailStore {
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Message>> storedMessages = new ConcurrentHashMap<>();
    private final HashMap<String, String> userCredentials; // need not be thread safe since readonly
    private int nextMessageId = 0;

    public MailStore(HashMap<String, String> userCredentials) {
        this.userCredentials = userCredentials;
    }

    public void storeMessage(Message message){
        for(var user : message.recipients){
            var username = user.split("@")[0];
            var userMap = getUserMessages(username);
            var id = getNextId().toString();
            userMap.put(id, message);
        }
    }

    public boolean hasUser(String username){
        return userCredentials.get(username) != null;
    }

    public boolean validateCredentials(DMAPServerModel.Credentials credentials){
        return credentials.password.equals(userCredentials.get(credentials.login));
    }

    public ConcurrentHashMap<String, Message> getUserMessages(String username){
        return storedMessages.compute(username, (__, value) -> value == null ? new ConcurrentHashMap<>() : value); // this is atomic
    }

    private synchronized Integer getNextId(){
        return nextMessageId++;
    }
}
