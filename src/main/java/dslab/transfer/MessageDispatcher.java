package dslab.transfer;

import dslab.data.monitoring.MonitoringPacket;
import dslab.util.Message;
import dslab.util.PacketSequence;
import dslab.util.dns.DNS;
import dslab.util.dns.DomainNameNotFoundException;
import dslab.util.tcp.TCPClient;
import dslab.util.tcp.TCPClientHandle;
import dslab.util.tcp.dmtp.DMTPClientModel;
import dslab.util.tcp.exceptions.ProtocolCloseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MessageDispatcher implements Runnable {

    private String idHost;
    private int idPort;
    private ExecutorService executor;
    private DatagramChannel monitoringChannel;
    private DNS dns = new DNS();

    private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    public MessageDispatcher(String idHost, int idPort, String monitoringHost, int monitoringPort, ExecutorService executor){
        this.idHost = idHost;
        this.idPort = idPort;
        this.executor = executor;

        try {
            monitoringChannel = DatagramChannel.open();
            monitoringChannel.connect(new InetSocketAddress(monitoringHost, monitoringPort));
            monitoringChannel.configureBlocking(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void queueMessage(Message message){
        messageQueue.add(message);
    }

    /**
     * Sends a message to the recipient's mailboxes and to the monitoring server for logging
     * @param message an incoming message
     */
    private void handleMessage(Message message) {
        reportMessageToMonitoring(message);

        var results = new ArrayList<CompletableFuture<String>>();

        // split recipients by domains
        var domains = message.getRecipientDomains();
        for(var domain : domains.entrySet()) {

            // build message only with recipients for domain
            var concreteMessage = message.clone();
            concreteMessage.recipients = domain.getValue().stream().map(user -> user + "@" + domain).collect(Collectors.toList());

            // try to send message
            try {
                results.add(sendMessageToMailbox(concreteMessage, domain.getKey()));
            } catch (DomainNameNotFoundException e) {

                // unknown domain; result is all users are unknown
                var result = new CompletableFuture<String>();
                var users = String.join(", ", domain.getValue());
                result.complete("error unknown domain " + domain.getKey() + " of " + users);
                results.add(result);
            }
            catch (IOException e) {

                // server domain is not available
                var result = new CompletableFuture<String>();
                var users = String.join(", ", domain.getValue());
                result.complete("error unavailable server " + domain.getKey() + " of " + users);

            }
        }

        // wait for results of all domain and if mailbox transfers failed, send the user a notification
        CompletableFuture.allOf(results.toArray(new CompletableFuture[0])).thenRun(() -> {
            List<String> errors = results.stream().map(result -> result.join()).filter(error -> error != null).collect(Collectors.toList());
            if(errors.size() > 0){
                String errorMessage = String.join("\n", errors);
                Message errorReport = new Message();
                errorReport.recipients = List.of(message.sender);
                errorReport.subject = "Failed to deliver message";
                errorReport.sender = "mail@" + idHost;
                errorReport.message = errorMessage + "\n> To: " + String.join(", ", message.recipients) + "\n> Subject: " + message.subject + "\n> " + message.message.replace("\n", "\n> ");
                try {
                    sendMessageToMailbox(errorReport, message.sender.split("@")[1]);
                } catch (Exception e) {
                    // further errors are ignored
                }
            }
        });
    }

    /**
     * sends an udp packet to the monitoring to collect stats about traffic
     * @param message the message which will be logged
     */
    private void reportMessageToMonitoring(Message message) {
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

    /**
     * opens a new connection to a mailbox server and transfers the message via dmtp to it;
     * mailbox server is parsed from the recipients and resolved through dns
     * the socket will run in a thread taken from the thread pool
     * @param message the message to transmit to the mailbox; contains only users of the target domain
     */
    private CompletableFuture<String> sendMessageToMailbox(Message message, String domain) throws DomainNameNotFoundException, IOException {
        InetSocketAddress mailboxAddress = null;
        mailboxAddress = dns.getDomainNameAddress(domain);
        var clientHandle = connectToTcpSocket(mailboxAddress.getHostName(), mailboxAddress.getPort());

        // set up receiver protocol (to validate server responses) and message sequence to be sent
        var client = clientHandle.getClient();
        var protocol = new DMTPClientModel();
        var sequence = PacketSequence.fromMessage(message);

        var result = new CompletableFuture<String>();

        client.onDataReceived = data -> {
            try {
                // first message should validate protocol or throw error
                if(!protocol.isValidated()) {
                    protocol.handle(data); // if this passes, server sent the protocol confirmation
                    client.send(sequence.next());
                }
                else {
                    // let sequence check returned data with expected data
                    sequence.verifyResponse(data, protocol);

                    // check if sequence has finished (or errored and remaining were cleared)
                    if(sequence.hasFinished()){
                        client.shutdown();
                        result.complete(null); //success
                    }

                    // go ahead with next packet in sequence
                    else {
                        client.send(sequence.next());
                    }
                }
            } catch (ProtocolCloseException e) {
                client.shutdown();
                result.complete(e.getResponsePacket().toPacketString());
            }
        };

        // start client after callbacks have initialized
        clientHandle.start();
        return result;
    }

    /**
     * creates a new client socket to a TCP server and runs it in the thread pool
     * @param host the target server host
     * @param port the target server TCP port
     * @return the client handle, containing the instance and a
     * callable to start the client in the thread pool
     */
    private TCPClientHandle connectToTcpSocket(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        var client = new TCPClient(socket);
        var handle = new TCPClientHandle(() -> executor.execute(client), client);
        return handle;
    }

    @Override
    public void run() {
        while(true){
            try {
                Message message = messageQueue.take();
                handleMessage(message);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
