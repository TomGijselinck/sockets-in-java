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
			System.err.println("Argument" + args[2] + " must be an integer");
			System.exit(1);
		}
		String HTTPversion = args[3];
		HTTPClient testClient = new HTTPClient(host, port);
		HTTPRequestMessage request = new HTTPRequestMessage(method,
				"/faq.png", HTTPversion);
		testClient.setHTTPRequestMessage(request);
		InputStream inFromServer = testClient.sendHTTPRequestMessage();
		// Read HTTP response message from the server, write it to the console
		// and write to a local file.
		testClient.parseHTTPMessage(inFromServer);

		// check headers
		System.out.println(testClient.getResponseMessage().getHeaderValue(
				"Content-Type"));

		// get embedded objects from the recieved HTTP response message body
		String html = testClient.getResponseMessage().getMessageBody();
		HTTPClient.getEmbeddedObjects(html);

	}

	public HTTPClient(String host, int port) {
		setHost(host);
		setPort(port);
		setWorkingDirectory("/home/tom/http");
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
	
	public Socket getCLientSOcket() {
		return clientSocket;
	}
	
	public void setClientSocket(Socket socket) {
		clientSocket = socket;
	}
	
	private Socket clientSocket;

	public String getWorkingDirectory() {
		return workingDirectory;
	}
	
	public void setWorkingDirectory(String directory) {
		workingDirectory = directory;
	}
	
	/**
	 * Variable referencing the workingdirectory in the filesystem of the system 
	 * where this HTTP client is running on. The working directory is used to
	 * store recieved files from the HTTP server.
	 */
	private String workingDirectory;

	/**
	 * Parse the HTTP response message from the server and store as a local
	 * file in the corresponding file tree.
	 */
	public void parseHTTPMessage(InputStream inFromServer)
			throws IOException {
		InputStreamReader inStreamReader = new InputStreamReader(inFromServer); 
		BufferedReader serverResponseText = new BufferedReader(inStreamReader);
		setResponseMessage(new HTTPResponseMessage(getRequestMessage()
				.getLocalPathRequest()));
		System.out.println("================RESPONSE================");
		responseMessage.setStatusLine(serverResponseText.readLine());
		System.out.println(responseMessage.getStatusLine());
		
		// parse message headers
		String response;
		while ((response = serverResponseText.readLine()) != null) {
			System.out.println(response);
			// check for the transition from the header part of the message to
			// the body part of the message.
			if (response.trim().equals("")) {
				break; // all headers are parsed
			}
			//TODO use different method than split
			String[] header = response.split(":");
			responseMessage.addAsHeader(header[0], header[1]);
		}
		
		// parse message body
		if (responseMessage.containsTextFile()) {
			System.out.println("NOTICE: message body is text");
			parseTextMessageBody(serverResponseText);
		} else if (responseMessage.containsBinaryFile()) {
			System.out.println("NOTICE: message body is binary data");
			parseBinaryMessageBody();
		}
		
		if (getRequestMessage().getHTTPVersion() == "HTTP/1.0") {
			clientSocket.close();
		}
	}

	public InputStream sendHTTPRequestMessage() throws IOException {
		HTTPRequestMessage httpRequest = getRequestMessage();
		// Create a socket to the given URI at the given port.
		clientSocket = new Socket(host, port);

		// Create an output stream (convenient data writer) to this host.
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());

		// Create an input stream (convenient data reader) to this host and wrap
		// a buffered input stream around the input stream for increased 
		// efficiency.
		InputStream inFromServer = clientSocket.getInputStream();

		// Compose HTTP request message and send to the server.
		outToServer.writeBytes(httpRequest.composeMessage());
		System.out.println("message send: \n" + httpRequest.composeMessage()
				+ "to " + host + ":" + port);
		
		return inFromServer;
	}

	private static void getEmbeddedObjects(String html) {
		// Use the Jsoup library to parse the html document
		Document doc = Jsoup.parse(html);

		// select all images from the parsed html document
		Elements links = doc.select("img[src]");
		for (Element link : links) {
			System.out.println("NESTED LINKS: " + link.attr("src"));
		}

	}
	
	private void parseTextMessageBody(BufferedReader inFromServer)
			throws IOException {
		File file = new File(workingDirectory
				+ responseMessage.getPathRequestedResource());
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		String response;
		while ((response = inFromServer.readLine()) != null) {
			System.out.println(response);
			responseMessage.addToMessageBody(response);
			bw.write(response + "\n");
		}
		bw.close();
		System.out.println("End of stream");

	}
	
	private void parseBinaryMessageBody()
			throws IOException {
//		InputStream inFromServer = clientSocket.getInputStream();
		InputStream inFromServer = sendHTTPRequestMessage();
		File file = new File(workingDirectory
				+ responseMessage.getPathRequestedResource());
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile());
		BufferedOutputStream outStream = new BufferedOutputStream(fos);
		BufferedInputStream inStream = new BufferedInputStream(inFromServer);
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		boolean endOfHeaderFound = false;
		int headerBytesRead;
		while ((bytesRead = inStream.read(buffer)) != -1) {
			headerBytesRead = 0;
			System.out.println(bytesRead);
			System.out.println(new String(buffer, 0, bytesRead));
			if (!endOfHeaderFound) {
				String string = new String(buffer, 0, bytesRead);
				int indexOfEndOfHeader = string.indexOf("\r\n\r\n");
				if (indexOfEndOfHeader != -1) {
					headerBytesRead = indexOfEndOfHeader + 4;
					System.out.println("headerbytes = " + headerBytesRead);
//					buffer = string.substring(indexOfEndOfHeader+4).getBytes();
					endOfHeaderFound = true;
				} else {
					bytesRead = 0;
				}
			}
			System.out.println("bytesRead = " + bytesRead);
			outStream.write(buffer, headerBytesRead, bytesRead - headerBytesRead);
			outStream.flush();
		}
		outStream.close();
		fos.close();
		System.out.println("End of stream");
	}

}