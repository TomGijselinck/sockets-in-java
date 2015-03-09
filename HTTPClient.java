import java.io.*;
import java.net.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTTPClient {

	public static void main(String[] args) throws Exception {
		HTTPMethod method = HTTPMethod.parseMethod(args[0]);
		String host = args[1];
		int port = 80;
		try {
			port = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.err.println("Argument" + args[2] + " must be an integer.");
			System.exit(1);
		}
		String HTTPversion = args[3];
		HTTPClient testClient = new HTTPClient(host, port);
		HTTPRequestMessage request = new HTTPRequestMessage(method, "/",
				HTTPversion);
		testClient.setHTTPRequestMessage(request);
		testClient.sendHTTPRequestMessage();

		// get embedded objects from the recieved HTTP response message body
		String html = testClient.getResponseMessage().getMessageBody();
		testClient.getEmbeddedObjects(html);

	}

	public HTTPClient(String host, int port) {
		setHost(host);
		setPort(port);
	}

	public HTTPRequestMessage getRequestMessage() {
		return requestMessage;
	}

	public void setHTTPRequestMessage(HTTPRequestMessage requestMessage) {
		this.requestMessage = requestMessage;
		requestMessage.setClient(this);
	}

	private HTTPRequestMessage requestMessage;

	public HTTPResponseMessage getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(HTTPResponseMessage responseMessage) {
		this.responseMessage = responseMessage;
	}

	private HTTPResponseMessage responseMessage;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	private String host;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private int port;

	/**
	 * Parse the HTTP response message from the server and store as a local
	 * file.
	 */
	public void parseHTTPMessage(BufferedReader serverResponse)
			throws IOException {
		File file = new File("/home/tom/http/index.html");
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		setResponseMessage(new HTTPResponseMessage());
		String response;
		boolean bodyMessage = false;
		boolean headerMessage = true;
		System.out.println("================RESPONSE================");
		responseMessage.setStatusLine(serverResponse.readLine());
		System.out.println(responseMessage.getStatusLine());
		while ((response = serverResponse.readLine()) != null) {
			System.out.println(response);
			if (response.trim().equals("")) {
				headerMessage = false;
				bodyMessage = true;
				continue;
			}
			if (headerMessage) {
				String[] header = response.split(":");
				responseMessage.addAsHeader(header[0], header[1]);
			} else if (bodyMessage) {
				responseMessage.addToMessageBody(response);
				bw.write(response + "\n");
			}
		}
		bw.close();
	}

	public void sendHTTPRequestMessage() throws IOException {
		HTTPRequestMessage httpRequest = getRequestMessage();
		DataOutputStream outToServer = null;
		BufferedReader inFromServer = null;
		// Create a socket to the given URI at the given port.
		Socket clientSocket = new Socket(host, port);

		// Create outputstream (convenient data writer) to this host.
		outToServer = new DataOutputStream(clientSocket.getOutputStream());

		// Create an inputstream (convenient data reader) to this host.
		inFromServer = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));

		// Compose HTTP request message and send to the server.
		outToServer.writeBytes(httpRequest.composeMessage());
		System.out.println("message send: \n" + httpRequest.composeMessage()
				+ "to " + host + ":" + port);

		// Read HTTP response message from the server, write it to the console
		// and write to a local file.
		parseHTTPMessage(inFromServer);
		if (httpRequest.getHTTPVersion() == "HTTP/1.0") {
			clientSocket.close();
		}
	}
	
	public void getEmbeddedObjects(String html) {
		// Use the Jsoup library to parse the html document
		Document doc = Jsoup.parse(html);
		
		// select all images from the parsed html document
		Elements links = doc.select("img[src]");
		for (Element link : links) {
			System.out.println("NESTED LINKS: " + link.attr("src"));
		}
		
	}

}