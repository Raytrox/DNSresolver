
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.*;
import java.net.*;

/**
 * 
 */

/**
 * @author Donald Acton This example is adapted from Kurose & Ross Feel free to
 *         modify and rearrange code as you see fit
 */
public class DNSlookup {

	static final int MIN_PERMITTED_ARGUMENT_COUNT = 2;
	static final int MAX_PERMITTED_ARGUMENT_COUNT = 3;

	static final int MAX_QUERY = 30;

	static final int ERR_NAME = -1;
	static final int ERR_TIMEOUT = -2;
	static final int ERR_LOOKUPEXCEED = -3;
	static final int ERR_OTHER = -4;
	static final int ERR_NOMATCHING = -6;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String fqdn;
		DNSResponse response;
		int argCount = args.length;
		boolean tracingOn = false;
		boolean IPV6Query = false;
		InetAddress rootNameServer;

		if (argCount < MIN_PERMITTED_ARGUMENT_COUNT || argCount > MAX_PERMITTED_ARGUMENT_COUNT) {
			usage();
			return;
		}

		rootNameServer = InetAddress.getByName(args[0]);
		fqdn = args[1];

		String askedFqdn = fqdn;

		if (argCount == 3) { // option provided
			if (args[2].equals("-t"))
				tracingOn = true;
			else if (args[2].equals("-6"))
				IPV6Query = true;
			else if (args[2].equals("-t6")) {
				tracingOn = true;
				IPV6Query = true;
			} else { // option present but wasn't valid option
				usage();
				return;
			}
		}
		try {
			InetAddress nameServer = rootNameServer;
			response = queryRecursion(nameServer, rootNameServer, fqdn, IPV6Query, tracingOn, 0);
			if ((response.getAnswers()[0].getIPaddress() != null)) {
				response.printResult(askedFqdn);
			}
		} catch (DNSLookupsExceededException e) {
			printFailure(askedFqdn, ERR_LOOKUPEXCEED, IPV6Query);
		} catch (DNSNameErrorException e) {
			printFailure(askedFqdn, ERR_NAME, IPV6Query);
		} catch (DNSNoCorrespondingIPException e) {
			printFailure(askedFqdn, ERR_NOMATCHING, IPV6Query);
		} catch (DNSTimeoutException e) {
			printFailure(askedFqdn, ERR_TIMEOUT, IPV6Query);
		} catch (Exception e) {
			printFailure(askedFqdn, ERR_OTHER, IPV6Query);
		}
	}

	// Recusive function that will return a DNSResponse that has the IP address for the asked domain (i.e. the
	// returned response has either the answer (ipv4 or ipv6) or no answer), the method will handle the case where cname is returned in the answer section
	private static DNSResponse queryRecursion(InetAddress nameServer, InetAddress rootNameServer, String fqdn,
			boolean IPV6, boolean tracingOn, int queryCount) throws Exception {
		DNSQuery query;
		byte[] received;
		DNSResponse response;

		while (true) {
			queryCount++;
			if (queryCount > MAX_QUERY) {
				throw new DNSLookupsExceededException(MAX_QUERY);
			}
			query = new DNSQuery(nameServer, fqdn, IPV6);
			received = sendQuery(query, tracingOn);
			response = new DNSResponse(received);
			if (tracingOn) {
				response.dumpResponse();
			}
			if (response.getAuthoritative() == true) {
				// case where the authoritative is true but cname is returned
				if (response.getAnswers()[0].getrType() == 0x05) {
					response = queryRecursion(rootNameServer, rootNameServer, response.getAnswers()[0].getFqdn(), IPV6,
							tracingOn, queryCount);
				}
				break;
			} else {
				if (response.nextServerinIPv4() == null) {
					response = queryRecursion(rootNameServer, rootNameServer, response.getNameservers()[0].getFqdn(),
							false, tracingOn, queryCount);
					nameServer = response.nextServerinIPv4();
				} else {
					nameServer = response.nextServerinIPv4();
				}
			}
		}
		return response;
	}

	// Take query as arguments and convert it to list of bytes value and send the packet
	private static byte[] sendQuery(DNSQuery query, Boolean tracingOn) throws IOException, DNSTimeoutException {
		// get a datagram socket
		DatagramSocket socket = new DatagramSocket();

		socket.setSoTimeout(5000);
		// send request
		byte[] buf = query.getQuery();
		byte[] received;
		received = new byte[1024];
		int retry = 0;

		InetAddress address = query.getDNSserver();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 53);
		DatagramPacket res;

		while (true) {
			try {
				if (tracingOn) {
					query.printQuery();
				}
				socket.send(packet);
				res = new DatagramPacket(received, received.length);
				socket.receive(res);
			} catch (SocketTimeoutException e) {
				if (retry < 1) {
					retry++;
					continue;
				} else {
					socket.close();
					throw new DNSTimeoutException();
				}
			}
			socket.close();
			break;
		}

		return res.getData();
	}

	// Helper function to print corresponding error
	private static void printFailure(String fqdn, int errCode, boolean IPV6) {
		if (IPV6) {
			System.out.println(fqdn + " " + errCode + " " + "AAAA" + " " + "0.0.0.0");
		} else {
			System.out.println(fqdn + " " + errCode + " " + "A" + " " + "0.0.0.0");
		}
	}

	private static void usage() {
		System.out.println("Usage: java -jar DNSlookup.jar rootDNS name [-6|-t|t6]");
		System.out.println("   where");
		System.out.println("       rootDNS - the IP address (in dotted form) of the root");
		System.out.println("                 DNS server you are to start your search at");
		System.out.println("       name    - fully qualified domain name to lookup");
		System.out.println("       -6      - return an IPV6 address");
		System.out.println("       -t      - trace the queries made and responses received");
		System.out.println("       -t6     - trace the queries made, responses received and return an IPV6 address");
	}
}
