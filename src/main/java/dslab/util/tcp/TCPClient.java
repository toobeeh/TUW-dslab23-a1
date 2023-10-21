package dslab.util.tcp;

import at.ac.tuwien.dsg.orvell.Shell;

import java.io.*;
import java.net.Socket;

public class TCPClient implements Runnable {

    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private PacketReceivedCallback callback;
    private boolean stopped = false;

    public TCPClient(Socket clientSocket, PacketReceivedCallback callback) throws IOException {
        this.clientSocket = clientSocket;
        this.callback = callback;
    }

    @Override
    public void run() {
        this.input = null;
        this.output = null;
        try {
            this.output = new PrintWriter(clientSocket.getOutputStream(), true);
            this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while(!this.stopped){
            try {
                this.callback.onPacketReceived(input.readLine());
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    public void shutdown(){
        this.stopped = true;
        try {
            this.input.close();
            this.output.close();
        } catch (IOException e) {
            System.err.println(e);
        }

        try {
            this.clientSocket.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public interface PacketReceivedCallback {
        void onPacketReceived(String data);
    }
}
