import java.net.InetAddress;
import java.net.UnknownHostException;

// Lots of the action associated with handling a DNS query is processing 
// the response. Although not required you might find the following skeleton of
// a DNSreponse helpful. The class below has a bunch of instance data that typically needs to be 
// parsed from the response. If you decide to use this class keep in mind that it is just a 
// suggestion.  Feel free to add or delete methods or instance variables to best suit your implementation.

public class DNSResponse {

    public static final int BITMASK = 0x01;
    public static final int SHORTMASK = 0xffff;
    public static final int TYPE_IPV4 = 1;
    public static final int TYPE_IPV6 = 28;
    public static final int TYPE_CN = 5;
    public static final int TYPE_NS = 2;
    public static final int CLASS_INTERNET = 1;

    private int queryID; // this is for the response it must match the one in the request
    private int answerCount = 0; // number of answers
    private boolean decoded = false; // Was this response successfully decoded
    private int nsCount = 0; // number of nscount response records
    private int additionalCount = 0; // number of additional (alternate) response records
    private boolean authoritative = false;// Is this an authoritative record

    private int QR;
    private int qCount;
    private int Rcode;
    private ResourceRecord answers[];
    private ResourceRecord nameServers[];
    private ResourceRecord Additionals[];
    private int currentIndex = 0;

    // Note you will almost certainly need some additional instance variables.

    // When in trace mode you probably want to dump out all the relevant information
    // in a response

    // print all resource records in the response 
    void dumpResponse() {

        System.out.println("Response ID: " + queryID + " Authoritative = " + authoritative);

        System.out.println("  Answers " + "(" + answerCount + ")");
        for (int i = 0; i < answerCount; i++) {
            answers[i].printRecord();
        }

        System.out.println("  Nameservers " + "(" + nsCount + ")");
        for (int i = 0; i < nsCount; i++) {
            nameServers[i].printRecord();
        }

        System.out.println("  Additional Information " + "(" + additionalCount + ")");
        for (int i = 0; i < additionalCount; i++) {
            Additionals[i].printRecord();
        }

    }

    // print the final result 
    void printResult(String askedFqdn) {
        for (int i = 0; i < answers.length; i++) {
            answers[i].printResult(askedFqdn);
        }
    }

    // Contruct a DNS response based on the bytes value 
    public DNSResponse(byte[] data)
            throws UnknownHostException, DNSNameErrorException, DNSNoCorrespondingIPException, Exception {

        queryID = readAndShift(data, currentIndex, 2);

        // extract query parameters, need to extract to helper method
        QR = data[currentIndex] >> 7 & BITMASK;
        if (QR != 1) {
            throw new Exception();
        }
        Rcode = data[currentIndex + 1] & 0xf;
        
        if ((data[currentIndex] & 0x4) != 0) {
            authoritative = true;
        }
        currentIndex += 2;

        if (Rcode == 3) {
            // dumpResponse();
            throw new DNSNameErrorException();
            // test case
        } else if (Rcode != 0) {
            // dumpResponse();
            throw new Exception();
        }

        // can be extract to a helper
        qCount = readAndShift(data, currentIndex, 2) & SHORTMASK;
        answerCount = readAndShift(data, currentIndex, 2) & SHORTMASK;
        nsCount = readAndShift(data, currentIndex, 2) & SHORTMASK;
        additionalCount = readAndShift(data, currentIndex, 2) & SHORTMASK;

        answers = new ResourceRecord[answerCount];
        nameServers = new ResourceRecord[nsCount];
        Additionals = new ResourceRecord[additionalCount];

        // need to read question first, the next 3 lines are only to probe to the first
        // answer
        decodeFQDN("", data, this.currentIndex);
        this.currentIndex += 4;

        for (int i = 0; i < answerCount; i++) {
            answers[i] = setResourceRecord(data);
        }

        for (int i = 0; i < nsCount; i++) {
            nameServers[i] = setResourceRecord(data);
        }

        for (int i = 0; i < additionalCount; i++) {
            Additionals[i] = setResourceRecord(data);
        }

        if (Rcode == 0 && authoritative == true && answers.length == 0) {
            // dumpResponse();
            throw new DNSNoCorrespondingIPException();
        }

        decoded = true;
    }

    public boolean getAuthoritative() {
        return authoritative;
    }

    public ResourceRecord[] getAdditionals() {
        return Additionals;
    }

    public ResourceRecord[] getNameservers() {
        return nameServers;
    }

    public ResourceRecord[] getAnswers() {
        return answers;
    }

    // Find the ipv4 address for the case where recursive query is needed 
    public InetAddress nextServerinIPv4() throws Exception {
        if (decoded) {
            if (answers.length > 0 && authoritative == true) {
                for (int i = 0; i < answers.length; i++) {
                    if (answers[i].getrType() == TYPE_IPV4) {
                        return answers[i].getIPaddress();
                    }
                }
                return null;
            } else {
                for (int i = 0; i < Additionals.length; i++) {
                    if (Additionals[i].getrType() == TYPE_IPV4) {
                        return Additionals[i].getIPaddress();
                    }
                }
                return null;
            }
        } else {
            throw new Exception();
        }
    }

    // Read the value in data and shift current index correspondingly 
    private int readAndShift(byte[] data, int offset, int length) {
        int val = 0;
        for (int i = 0; i < length; i++) {
            int temp = (int) (data[offset + i] & 0xff);
            val = val | (temp << (length - 1 - i) * 8);
        }
        this.currentIndex += length;
        return val;
    }

    // Assume that currentIndex is at the first byte of response records, create resource records based on the byte value 
    private ResourceRecord setResourceRecord(byte[] data) throws UnknownHostException, Exception {
        String fqdn = "";
        fqdn = decodeFQDN(fqdn, data, currentIndex);
        int rType = readAndShift(data, currentIndex, 2);
        int rClass = readAndShift(data, currentIndex, 2);
        int ttl = readAndShift(data, currentIndex, 4);
        int rdLength = readAndShift(data, currentIndex, 2);

        if (rClass == CLASS_INTERNET) {
            // address case, could be ipv4 (1) or ipv6 (28)
            if (rType == TYPE_IPV4 || rType == TYPE_IPV6) {
                byte[] address = new byte[rdLength];
                for (int i = 0; i < rdLength; i++) {
                    address[i] = data[currentIndex];
                    currentIndex++;
                }
                return new ResourceRecord(fqdn, rType, rClass, ttl, null, InetAddress.getByAddress(address));
            } else if (rType == TYPE_NS || rType == TYPE_CN) {
                // 2 for name server, 5 for cname
                String rdataFqdn = decodeFQDN("", data, currentIndex);
                return new ResourceRecord(fqdn, rType, rClass, ttl, rdataFqdn, null);
            }
        } else {
            throw new Exception();
        }
        return null;
    }

    // Recursively decode the domain names
    private String decodeFQDN(String fqdn, byte[] data, int offset) {
        while (data[offset] != 0) {
            if ((data[offset] & 0xff) >= 0xc0) {
                int temp = readAndShift(data, offset, 2) & 0x3fff;
                fqdn = decodeFQDN(fqdn, data, temp);
                this.currentIndex = offset + 2;
                return fqdn;
            }
            int len = (int) data[offset] & 0xff;
            for (int i = 0; i < len; i++) {
                offset++;
                fqdn = fqdn + (char) data[offset];
            }
            offset++;
            fqdn += ".";
        }
        this.currentIndex = offset + 1;
        fqdn = fqdn.substring(0, fqdn.length() - 1);
        return fqdn;
    }

}
