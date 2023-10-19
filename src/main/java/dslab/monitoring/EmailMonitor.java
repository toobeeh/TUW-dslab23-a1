package dslab.monitoring;

import dslab.data.monitoring.MonitoringPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EmailMonitor {

    private Map<String, Integer> addresses = new HashMap<>();
    private Map<String, Integer> servers = new HashMap<>();

    public void log(MonitoringPacket packet){

        // increment email count
        this.addresses.compute(packet.email, (key, value) -> value == null ? 1 : value + 1);

        // increment server count
        String server = packet.host + ":" + packet.port;
        this.servers.compute(server, (key, value) -> value == null ? 1 : value + 1);
    }

    public String getServersResult(){
        return this.servers
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    public String getAddressesResult(){
        return this.addresses
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }
}
