package dslab.util.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * A thread that establishes a read-only UDP socket
 * This class extends thread; UDPReceiver::start has to be called in order to start listening
 */
public class UDPReceiver extends Thread {

    private final DatagramSocket socket;
    private final PacketReceivedCallback callback;
    private final int packetSize;

    public UDPReceiver(int port, int packetSize, PacketReceivedCallback callback) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.packetSize = packetSize;
        this.callback = callback;
    }

    /**
     * Starts listening for udp packets on the specified port in the own thread
     */
    @Override
    public void run() {

        this.setName("UDP Receiver");

        byte[] receivedData = new byte[packetSize];

        while(!isInterrupted()) {

            // try to receive packet
            DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);
            try {
                socket.receive(packet);
            }
            catch (IOException e) {
                // failed to receive packet
                continue;
            }

            // get packet data and do callback
            String data = new String(packet.getData(), 0, packet.getLength());
            InetAddress senderAddress = packet.getAddress();

            callback.onPacketReceivedCallback(data, senderAddress);
        }
    }

    /**
     * Stops receiving udp packets and shuts down the thread
     */
    public void shutdown() {
        interrupt();
        if(!socket.isClosed()) socket.close();
    }

    public interface PacketReceivedCallback {
        void onPacketReceivedCallback(String data, InetAddress senderAddress);
    }

}
