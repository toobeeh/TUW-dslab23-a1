package dslab.transfer;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

import dslab.ComponentFactory;
import dslab.data.dmtp.MessagePacket;
import dslab.util.Config;

public class TransferServer implements ITransferServer, Runnable {

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out) {
        // TODO
    }

    @Override
    public void run() {
        // TODO
    }

    @Override
    public void shutdown() {
        // TODO
    }

    public static void main(String[] args) throws Exception {

        try{

            var parser = new DMTPParser();
            var result = parser.parse("subject hi");

            System.out.println(result);
        }
        catch (Exception e){
            System.err.println(e);
        }

        //ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        //server.run();
    }

}
