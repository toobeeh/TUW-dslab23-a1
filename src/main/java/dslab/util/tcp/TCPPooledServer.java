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
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    public TCPPooledServer(int port, WorkerFactory workerFactory){
        this.port = port;
        this.workerFactory = workerFactory;
    }

    @Override
    public void run() {
        this.thread = Thread.currentThread();
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while(!thread.isInterrupted()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(this.thread.isInterrupted()) {
                    break;
                }
                System.err.println(e);
            }
            this.threadPool.execute(this.workerFactory.createWorker(clientSocket));
        }

        this.threadPool.shutdown();
    }

    /**
     * creates a new client socket to a TCP server and runs it in the thread pool
     * @param host the target server host
     * @param port the target server TCP port
     * @return the client handle, containing the instance and a
     * callable to start the client in the thread pool
     */
    public TCPClientHandle createNewTCPSocket(String host, int port){
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var client = new TCPClient(socket);
        var handle = new TCPClientHandle(() -> threadPool.execute(client), client);
        return handle;
    }

    public void shutdown() {
        if(!this.serverSocket.isClosed()) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        if (this.thread != null) {
            this.thread.interrupt();
            try {
                this.thread.join();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }

    public interface WorkerFactory {
        Runnable createWorker(Socket clientSocket);
    }
}

