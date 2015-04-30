package sir.barchable.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sir Barchable
 *         Date: 16/04/15
 */
public class Dns {
    private static final Logger log = LoggerFactory.getLogger(Dns.class);

    private String server;

    public Dns(String server) {
        this.server = server;
    }

    public InetAddress getAddress(String hostName) throws UnknownHostException {
        InetAddress address = getAllAddresses(hostName).get(0);
        log.debug("Resolved {} to {}", hostName, address.getHostAddress());
        return address;
    }

    public List<InetAddress> getAllAddresses(String hostName) throws UnknownHostException {
        Lookup lookup;
        try {
            lookup = new Lookup(hostName, Type.A);
            lookup.setCache(null);
            lookup.setResolver(new SimpleResolver(server));
        } catch (TextParseException e) {
            throw new UnknownHostException(hostName);
        }

        List<InetAddress> addresses = new ArrayList<>();

        Record[] a = lookup.run();
        if (a == null || a.length == 0) {
            throw new UnknownHostException(hostName);
        }

        for (Record record : a) {
            addresses.add(((ARecord) record).getAddress());
        }

        return addresses;
    }
}
