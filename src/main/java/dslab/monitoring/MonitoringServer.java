package dslab.monitoring;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.data.monitoring.MonitoringPacket;
import dslab.util.Config;
import dslab.util.udp.UDPReceiver;

public class MonitoringServer implements IMonitoringServer {

    private UDPReceiver receiver;
    private EmailMonitor monitor;
    private Shell shell;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MonitoringServer(String componentId, Config config, InputStream in, PrintStream out) throws SocketException {
        int port = config.getInt("udp.port");
        this.monitor = new EmailMonitor();

        this.receiver = new UDPReceiver(port, 256, (message, address) -> {
            MonitoringPacket packet = new MonitoringPacket().parseString(message);
            this.monitor.log(packet);
        });

        this.shell = new Shell(in, out);
        this.shell.setPrompt(componentId + "> ");
        this.shell.register(this);
    }

    @Override
    public void run() {
        this.receiver.listen();
        this.shell.run();
    }

    @Override
    @Command
    public void addresses() {
        String addresses = this.monitor.getAddressesResult();
        this.shell.out().println(addresses.length() == 0 ? "No mails logged yet." : addresses);
    }

    @Override
    @Command
    public void servers() {
        String servers = this.monitor.getServersResult();
        this.shell.out().println(servers.length() == 0 ? "No mails logged yet." : servers);
    }

    @Override
    @Command
    public void shutdown() {
        this.receiver.stop();
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        IMonitoringServer server = ComponentFactory.createMonitoringServer(args[0], System.in, System.out);
        server.run();
    }

}
