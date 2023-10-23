package dslab.util.dns;

import dslab.util.Config;

import java.net.InetSocketAddress;

public class DNS {

    private Config config;
    public DNS(){
        this.config = new Config("domains");
    }

    public InetSocketAddress getDomainNameAddress(String domainName) throws DomainNameNotFoundException {
        var config = new Config("domains");
        if(!config.containsKey(domainName)) throw new DomainNameNotFoundException();

        var uri = config.getString(domainName);
        var host = uri.split(":")[0];
        var port = Integer.parseInt(uri.split(":")[1]);

        var address = new InetSocketAddress(host, port);
        return address;
    }
}
