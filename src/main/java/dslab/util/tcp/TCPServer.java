package dslab.util.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * A runnable that starts a new tcp server socket
 * and starts listening for new cients.
 * Each client gets a new thread from the provided executor service;
 * the client runnables are created by the provided worker factory
 */
public class TCPServer implements Runnable {
    private ServerSocket serverSocket = null;
    private final WorkerFactory workerFactory;
    private final int port;
    private Thread thread;
    private final ExecutorService threadPool;

    public TCPServer(int port, WorkerFactory workerFactory, ExecutorService executorService){
        this.port = port;
        this.workerFactory = workerFactory;
        this.threadPool = executorService;
    }

    /**
     * Starts the server socket and waits for clients,
     * as long as the thread is not interrupted
     */
    @Override
    public void run() {
        this.thread = Thread.currentThread();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while(!thread.isInterrupted()){
            Socket clientSocket;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                break;
            }
            threadPool.execute(workerFactory.createWorker(clientSocket));
        }
    }

    /**
     * Interrupts the current thread and therefore won't accept new clients.
     * Closes the server socket.
     */
    public void shutdown() {
        if (thread != null) {
            thread.interrupt();
        }
        if(!serverSocket.isClosed()) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public interface WorkerFactory {
        Runnable createWorker(Socket clientSocket);
    }
}

