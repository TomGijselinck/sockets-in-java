import java.io.*;
import java.net.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class TCPClient {
	
	public static void main(String[] args)  throws Exception {
		TCPClient testClient = new TCPClient();
		String command = args[0];
		String host = args[1];
		int port = 80;
		try {
			port = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.err.println("Argument" + args[2] + " must be an integer.");
	        System.exit(1);
		}
		String HTTPversion = args[3];
		DataOutputStream outToServer = null;
		BufferedReader inFromServer = null;
		
		
		// Create a socket to the given URI at the given port (default port 80).
		Socket clientSocket = new Socket(host, port);
		
		// Create outputstream (convenient data writer) to this host.
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
		// Create an inputstream (convenient data reader) to this host.
		inFromServer = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));
		
		// Compose HTTP message and send to server.
		String message = command + " / " + HTTPversion + "\r\n" + 
						 "Host: " + host + "\r\n\r\n";
		outToServer.writeBytes(message);
		System.out.println("message send: \n" + message + "to " + host + ":" 
							+ port);
		
		// Read text from the server, write it to the console and save to a
		// local file.
		testClient.parseHTTPMessage(inFromServer);
				
		// Use the Jsoup library to parse document from response
		System.out.println(testClient.getHTTPResponseMessage().getMessageBody());
		Document doc = Jsoup.parse(testClient.getHTTPResponseMessage()
				.getMessageBody());
		Elements links = doc.select("a[href]");
		for(Element link : links) {
			 System.out.println("NESTED LINKS: " + link.attr("abs:href"));
		}
		
		// Close socket
		clientSocket.close();
	}
	
	public TCPClient() {
		//TODO: work out
		this.HTTPResponseMessage = new HTTPMessage();
	}
	
	/*
	 * Parse the HTTP message response from the server and store as a local 
	 * file.
	 */
	public void parseHTTPMessage(BufferedReader serverResponse) 
			throws IOException {
		File file = new File("/home/tom/response.html");
		if(!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		String response;
		boolean bodyMessage = false;
		boolean headerMessage = true;
		System.out.println("================RESPONSE================");
		HTTPResponseMessage.setInitialLine(serverResponse.readLine());
		System.out.println(HTTPResponseMessage.getInitialLine());
		while((response = serverResponse.readLine()) != null) {
			System.out.println(response);
			if(response.trim().equals("")) {
				headerMessage = false;
				bodyMessage = true;
				continue;
			}
			if(headerMessage) {
				String[] header = response.split(":");
				HTTPResponseMessage.addAsHeader(header[0], header[1]);
			} else if(bodyMessage) {
				HTTPResponseMessage.addToMessageBody(response);
				bw.write(response + "\n");
			}			
		}
		bw.close();
	}
	
	public HTTPMessage getHTTPResponseMessage() {
		return HTTPResponseMessage;
	}
	
	private HTTPMessage HTTPResponseMessage;

}