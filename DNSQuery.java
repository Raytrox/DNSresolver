import java.net.InetAddress;
import java.util.Random;
import java.util.Arrays;

public class DNSQuery {

    private int queryID;
    private String domainName;
    private InetAddress DNSserver;
    private byte[] query;
    private int packetLength;
    private boolean IPv6;


    public DNSQuery(InetAddress rootNameServer, String fqdn, Boolean ipv6){
        domainName = fqdn;
        queryID = generateID();
        DNSserver = rootNameServer;
        IPv6 = ipv6;
        buildQuery(ipv6);
    }

    private void buildQuery(Boolean ipv6) {
        query = new byte[512]; 
        // Assume that no query is longer than 512 bytes

        query[0] = (byte) (queryID >> 8);
        query[1] = (byte) queryID;
        // set query ID

        query[2] = 0;
        query[3] = 0;

        query[4] = 0;
        query[5] = 1;

        query[6] = 0;
        query[7] = 0;
        // answer count

        query[8] = 0;
        query[9] = 0;
        // NScount

        query[10] = 0;
        query[11] = 0;
        // ARcount

        int currentIndex = 12;
        String[] words = domainName.split("\\.");

        for (int i = 0; i < words.length; i++) {
            query[currentIndex] = (byte) words[i].length();
            currentIndex++;
            for (int j = 0; j < words[i].length(); j++) {
                query[currentIndex] = (byte) words[i].charAt(j);
                currentIndex++;
            }
        }
        query[currentIndex++] = 0;
        // indicate the end of the question

        query[currentIndex++] = 0;
        if (ipv6) {
            query[currentIndex++] = 28;
        } else {
            query[currentIndex++] = 1;
        }

        query[currentIndex++] = 0;
        query[currentIndex++] = 1;
        // set Qclass to Internet address

        query = Arrays.copyOfRange(query, 0, currentIndex);
        // set the length of query
        packetLength = currentIndex;
        
    }

    private int generateID() {
        Random r = new Random();
        int val = (r.nextInt(65535) + 1);
        return val;
    }

    void printQuery() {
        System.out.printf("%n%n");
        // printing 2 blank lines
        if (IPv6) {
            System.out.println("Query ID" + "     " + queryID + " " + domainName + "  " + "AAAA" + " --> " + DNSserver.getHostAddress());
        } else {
            System.out.println("Query ID" + "     " + queryID + " " + domainName + "  " + "A" + " --> " + DNSserver.getHostAddress());
        }
    }

    public int getPacketLength() {
        return packetLength;
    }

    public InetAddress getDNSserver() {
        return DNSserver;
    }

    public byte[] getQuery() {
        return query;
    }
}