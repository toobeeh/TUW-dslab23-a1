package dslab.util.tcp;

import dslab.data.Packet;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TCPClient implements Runnable {

    private final Socket clientSocket;
    private PrintWriter output;
    private final CompletableFuture<TCPClient> onSocketShutdown = new CompletableFuture<>();
    private final CompletableFuture<TCPClient> onSocketReady = new CompletableFuture<>();
    private boolean stopped = false;
    public Consumer<String> onDataReceived = null;
    public CompletableFuture<TCPClient> getOnSocketShutdown() {
        return onSocketShutdown;
    }
    public CompletableFuture<TCPClient> getOnSocketReady() {
        return onSocketReady;
    }

    public TCPClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        BufferedReader input;
        output = null;
        try {
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        onSocketReady.complete(this);

        while(!stopped){
            try {
                var line = input.readLine();
                if(line == null) shutdown();
                else if(onDataReceived != null) {
                    System.out.println(clientSocket.getInetAddress().toString() + " >>> " + line);
                    onDataReceived.accept(line);
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        shutdown();
    }

    public void send(String data) {
        System.out.println(clientSocket.getInetAddress().toString() + " <<< " + data);
        output.println(data);
    }

    public void send(Packet packet) {
        this.send(packet.toPacketString());
    }

    public synchronized void shutdown(){
        if(stopped) return;
        stopped = true;

        try {
            if(!clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        onSocketShutdown.complete(this);
    }
}
