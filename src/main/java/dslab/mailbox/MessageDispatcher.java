package dslab.mailbox;

import dslab.data.dmtp.ErrorPacket;
import dslab.data.monitoring.MonitoringPacket;
import dslab.util.PacketSequence;
import dslab.util.dns.DNS;
import dslab.util.dns.DomainNameNotFoundException;
import dslab.util.tcp.TCPClient;
import dslab.util.tcp.TCPClientHandle;
import dslab.util.tcp.dmtp.DMTPClientModel;
import dslab.util.tcp.dmtp.DMTPServerModel;
import dslab.util.tcp.exceptions.ProtocolCloseException;
import dslab.util.udp.UDPSender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;

public class MessageDispatcher {

    private String idHost;
    private int idPort;
    private ExecutorService executor;
    private DatagramChannel monitoringChannel;
    private DNS dns = new DNS();

    public MessageDispatcher(String idHost, int idPort, String monitoringHost, int monitoringPort, ExecutorService executor){
        this.idHost = idHost;
        this.idPort = idPort;
        this.executor = executor;

        try {
            this.monitoringChannel = DatagramChannel.open();
            this.monitoringChannel.connect(new InetSocketAddress(monitoringHost, monitoringPort));
            this.monitoringChannel.configureBlocking(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleMessage(DMTPServerModel.Message message) {
        reportMessageToMonitoring(message);
        sendMessageToMailbox(message);
    }

    private void reportMessageToMonitoring(DMTPServerModel.Message message) {
        MonitoringPacket monitoring = new MonitoringPacket();
        monitoring.port = idPort;
        monitoring.host = idHost;
        monitoring.email = message.sender;
        try {
            monitoringChannel.send(ByteBuffer.wrap(monitoring.toPacketString().getBytes()), monitoringChannel.getRemoteAddress());
        } catch (IOException e) {
            // will not be handled
        }
    }

    private void sendMessageToMailbox(DMTPServerModel.Message message) {
        InetSocketAddress mailboxAddress = null;
        try {
            mailboxAddress = dns.getDomainNameAddress("earth.planet");
        } catch (DomainNameNotFoundException e) {
            throw new RuntimeException("could not be delivered blah"); // TODO call reporting function
        }
        var clientHandle = connectToTcpSocket(mailboxAddress.getHostName(), mailboxAddress.getPort());

        var client = clientHandle.getClient();
        var protocol = new DMTPClientModel();
        var sequence = PacketSequence.fromMessage(message);

        client.onDataReceived = data -> {
            try {
                // if protocol not validated, wait until server sent begin
                if(!protocol.isValidated()) {
                    var response = protocol.handle(data); // if this passes, server sent the protocol confirmation
                    client.send(sequence.next());
                }
                else {
                    // let sequence handle next packet
                    var result = sequence.checkResponse(data, protocol);

                    if(sequence.hasFinished()){
                        client.shutdown();

                        if(result instanceof ErrorPacket){
                            throw new RuntimeException("could not be delivered blah"); // TODO call reporting function
                        }
                    }
                    else {
                        client.send(sequence.next());
                    }
                }
            } catch (ProtocolCloseException e) {
                client.shutdown();
                throw new RuntimeException("could not be delivered blah"); // TODO call reporting function
            }
        };

        clientHandle.start();
    }

    /**
     * creates a new client socket to a TCP server and runs it in the thread pool
     * @param host the target server host
     * @param port the target server TCP port
     * @return the client handle, containing the instance and a
     * callable to start the client in the thread pool
     */
    private TCPClientHandle connectToTcpSocket(String host, int port){
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var client = new TCPClient(socket);
        var handle = new TCPClientHandle(() -> executor.execute(client), client);
        return handle;
    }

}
