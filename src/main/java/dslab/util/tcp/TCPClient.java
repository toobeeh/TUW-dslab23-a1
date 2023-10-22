package dslab.util.tcp;

import dslab.data.Packet;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TCPClient implements Runnable {

    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    public Consumer<String> onDataReceived = null;
    public Runnable onSocketShutdown = null;
    public Runnable onSocketReady = null;
    private boolean stopped = false;

    public TCPClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
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

        if(onSocketReady != null) onSocketReady.run();

        while(!this.stopped){
            try {
                var line = input.readLine();
                if(onDataReceived != null) onDataReceived.accept(line);
            } catch (IOException e) {
                this.shutdown();
            }
        }
    }

    public void send(String data) {
        this.output.println(data);
    }

    public void send(Packet packet) {
        this.output.println(packet.toPacketString());
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
        if(onSocketShutdown != null) onSocketShutdown.run();
    }
}
