package dslab.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("MethodDoesntCallSuperMethod")
public class Message {
    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+)";
    public static boolean isValidEmail(String email){
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.find();
    }

    public String subject;
    public String sender;
    public String message;
    public List<String> recipients;

    /**
     * maps the message's recipients by their domain
     * emails need to be validated before with isValidEMail
     * @return returns a map containing domains as keys and their recievers as values
     */
    public Map<String, List<String>> getRecipientDomains(){
        Map<String, List<String>> domainMap = new HashMap<>();
        for(var recipient : recipients) {
            var domain = recipient.split("@")[1];
            var user = recipient.split("@")[0];
            domainMap.compute(domain, (key, users) -> {
                if(users == null) return new ArrayList<>(List.of(user));
                else {
                    users.add(user);
                    return users;
                }
            });
        }
        return domainMap;
    }

    public Message clone() {
        Message m = new Message();
        m.message = message;
        m.subject = subject;
        m.sender = sender;
        m.recipients = List.copyOf(recipients);
        return m;
    }
}
