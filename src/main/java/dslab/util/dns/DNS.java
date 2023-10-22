package dslab.util.dns;

import dslab.util.Config;

public class DNS {

    private Config config;
    public DNS(){
        this.config = new Config("domains");
    }

    public String getDomainNameAddress(String domainName) throws DomainNameNotFoundException {
        var config = new Config("domains");
        if(!config.containsKey(domainName)) throw new DomainNameNotFoundException();
        return config.getString(domainName);
    }
}
