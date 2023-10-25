package dslab.util.tcp;

public class TCPClientHandle {
    private final Runnable startRunner;
    private final TCPClient client;

    public TCPClientHandle(Runnable start, TCPClient client) {
        this.startRunner = start;
        this.client = client;
    }

    /**
     * starts the tcp client in the thread pool
     */
    public void start() {
        startRunner.run();
    }

    /**
     * @return the client of this handle
     */
    public TCPClient getClient() {
        return client;
    }
}
