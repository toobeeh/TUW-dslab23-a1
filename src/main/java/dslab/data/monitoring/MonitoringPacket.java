package dslab.data.monitoring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonitoringPacket {

    public String host;
    public int port;
    public String email;

    @Override
    public String toString() {
        return this.host + ":" + this.port + " " + this.email;
    }

    public static MonitoringPacket fromString(String data){

        // build regex to match data format <host>:<port> <email-address>
        Pattern pattern = Pattern.compile("(\\S+):(\\d+) (\\S+)");
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            MonitoringPacket packet = new MonitoringPacket();
            packet.host = matcher.group(1);
            packet.port = Integer.parseInt(matcher.group(2));
            packet.email = matcher.group(3);

            return packet;
        } else {
            throw new IllegalArgumentException("Invalid data format");
        }
    }
}
