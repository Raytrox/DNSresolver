import java.net.InetAddress;
import java.util.jar.Attributes.Name;

public class ResourceRecord {

    private String Name;
    private int rType;
    private int rClass;
    private int TTL;
    // private short RdataLength;
    private String fqdn;
    private InetAddress IPaddress;
    // Rdata can either be an ip address or fqdn (depending on the type)

    // sent the original data since Name and Rdata may have pointer refer to the previous part
    public ResourceRecord(String name, int type, int c, int ttl, String dn, InetAddress address){
        Name = name; 
        rType = type; // can be ipv4, ipv6, nameserver or cname
        rClass = c;
        TTL = ttl;
        fqdn = dn;
        IPaddress = address;
    }

    public String getName() {
        return Name;
    }

    public int getrType() {
        return rType;
    }

    public int getrClass() {
        return rClass;
    }

    public long getTTL() {
        return TTL;
    }

    public String getFqdn() {
        return fqdn;
    }

    public InetAddress getIPaddress() {
        return IPaddress;
    }

    public void printRecord() {
        switch (rType) {
            case 1: 
                // ipv4
                System.out.printf("       %-31s%-11d%-5s%-20s%n", Name, TTL, "A", IPaddress.getHostAddress());
                break;
            case 2:
                // name server
                System.out.printf("       %-31s%-11d%-5s%-20s%n", Name, TTL, "NS", fqdn);
                break;
            case 5:
                // cname
                System.out.printf("       %-31s%-11d%-5s%-20s%n", Name, TTL, "CN", fqdn);
                break;
            case 28:
                // ipv6 address
                System.out.printf("       %-31s%-11d%-5s%-20s%n", Name, TTL, "AAAA", IPaddress.getHostAddress());
                break;
        }
    }

    public void printResult(String askedFqdn) {
        switch (rType) {
            case 1: 
                System.out.println(askedFqdn + "  " + TTL + "    " + "A " + IPaddress.getHostAddress());
                break;
            case 28: 
                System.out.println(askedFqdn + "  " + TTL + "    " + "AAAA " + IPaddress.getHostAddress());
                break;
        }
    }

}