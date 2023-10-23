package dslab.util.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * A class that establishes a read-only UDP socket
 */
public class UDPReceiver implements Runnable {

    private DatagramSocket socket;
    private Thread thread;
    private PacketReceivedCallback callback;
    private int packetSize;
    private boolean isRunning = false;

    public UDPReceiver(int port, int packetSize, PacketReceivedCallback callback) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.packetSize = packetSize;
        this.callback = callback;
    }

    public void listen() {
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run() {

        byte[] receivedData = new byte[packetSize];

        while(!thread.isInterrupted()) {

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

            try{
                callback.onPacketReceivedCallback(data, senderAddress);
            }
            catch (Exception e) {
                System.err.println("Error in Callback: \n" + e.toString());
            }
        }
    }

    public void stop() {
        if(!socket.isClosed()) socket.close();
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) { }
        }
    }

    public interface PacketReceivedCallback {
        void onPacketReceivedCallback(String data, InetAddress senderAddress);
    }

}
