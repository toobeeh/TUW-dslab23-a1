package dslab.util.dns;

import dslab.util.Config;

import java.net.InetSocketAddress;

public class DNS {

    private final Config config = new Config("domains");
    public DNS(){ }

    public InetSocketAddress getDomainNameAddress(String domainName) throws DomainNameNotFoundException {
        if(!config.containsKey(domainName)) throw new DomainNameNotFoundException();

        var uri = config.getString(domainName);
        var host = uri.split(":")[0];
        var port = Integer.parseInt(uri.split(":")[1]);

        return new InetSocketAddress(host, port);
    }
}
