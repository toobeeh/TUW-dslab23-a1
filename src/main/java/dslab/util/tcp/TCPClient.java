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

    public CompletableFuture<TCPClient> getOnSocketShutdown() {
        return onSocketShutdown;
    }

    public CompletableFuture<TCPClient> getOnSocketReady() {
        return onSocketReady;
    }

    private CompletableFuture<TCPClient> onSocketShutdown = new CompletableFuture<>();
    private CompletableFuture<TCPClient> onSocketReady = new CompletableFuture<>();
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

        if(onSocketReady != null) onSocketReady.complete(this);

        while(!this.stopped){
            try {
                var line = input.readLine();
                if(line == null) this.shutdown();
                else if(onDataReceived != null) {
                    System.out.println(clientSocket.getInetAddress().toString() + " >>> " + line);
                    onDataReceived.accept(line);
                }
            } catch (IOException e) {
                this.shutdown();
            }
        }

        shutdown();
    }

    public void send(String data) {
        System.out.println(clientSocket.getInetAddress().toString() + " <<< " + data);
        this.output.println(data);
    }

    public void send(Packet packet) {
        this.send(packet.toPacketString());
    }

    public synchronized void shutdown(){
        if(stopped) return;
        this.stopped = true;

        try {
            this.input.close();
            this.output.close();
        } catch (IOException e) {
            System.err.println(e);
        }

        try {
            if(!clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println(e);
        }
        if(onSocketShutdown != null) onSocketShutdown.complete(this);
    }
}
