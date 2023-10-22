package dslab.util.udp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

/**
 * A class that establishes a write-only connection to a UDP receiver
 */
public class UDPSender {

    private DatagramSocket socket;
    private InetAddress receiverAddress;
    private int receiverPort;

    public UDPSender(String receiverHost, int receiverPort) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        this.receiverAddress = InetAddress.getByName(receiverHost);
        this.receiverPort = receiverPort;
    }

    public void send(String message) {
        try {
            byte[] buffer = message.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.receiverAddress, this.receiverPort);
            this.socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
