package dslab.util.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPPooledServer implements Runnable {
    private ServerSocket serverSocket = null;
    private WorkerFactory workerFactory;
    private int port;
    private Thread thread;
    private ExecutorService threadPool;

    public TCPPooledServer(int port, WorkerFactory workerFactory, ExecutorService executorService){
        this.port = port;
        this.workerFactory = workerFactory;
        this.threadPool = executorService;
    }

    @Override
    public void run() {
        this.thread = Thread.currentThread();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while(!thread.isInterrupted()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                System.err.println(e);
                break;
            }
            threadPool.execute(workerFactory.createWorker(clientSocket));
        }
    }

    public void shutdown() {
        if(!serverSocket.isClosed()) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }

    public interface WorkerFactory {
        Runnable createWorker(Socket clientSocket);
    }
}

