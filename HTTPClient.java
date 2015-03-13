import java.io.*;
import java.net.*;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTTPClient {

	public static void main(String[] args) throws Exception {
		// Parse all given arguments.
		HTTPMethod method = HTTPMethod.parseMethod(args[0]);
		if (!args[1].contains("http")) {args[1] = "http://" + args[1];}
		URI uri = new URI(args[1]);
		int port = 80;
		try {
			port = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.err.println("Argument" + args[2] + " must be an integer");
			System.exit(1);
		}
		String HTTPversion = args[3];
		
		// Create an instance of this class to enable bidirectional 
		// communication using HTTP.
		HTTPClient testClient = new HTTPClient(uri.getHost(), port);
		
		// Create the first request message using the given arguments.
		HTTPRequestMessage request = new HTTPRequestMessage(method,
				"/index.html", HTTPversion);
		request.addAsHeader("Host", testClient.getHost());
		if (method.equals(HTTPMethod.GET)) {
			File file = new File(testClient.workingDirectory + "/"
					+ testClient.host + "/index.html");
			if (file.exists()) {
				request.setIfModifiedSinceHeader(new Date(file.lastModified()));
			}
		}
		testClient.setHTTPRequestMessage(request);
		
		// Send the constructed HTTP request message and create an input stream
		// for receiving the response message from the server.
		testClient.setClientSocket(new Socket(testClient.getHost(), testClient
				.getPort())); //TODO zonder argumenten
		InputStream inFromServer = testClient.sendHTTPRequestMessage();
		
		// Read HTTP response message from the server, write it to the console
		// and if the message contains a message body, write this message body 
		// to a local file.
		testClient.parseHTTPMessage(inFromServer);

		// Get embedded objects from the received HTTP response message body if 
		// it exists.
		String html = testClient.getResponseMessage().getMessageBody();
		if (!html.isEmpty()) testClient.getEmbeddedObjects(html);
		

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
	 * Variable referencing the working directory in the filesystem of the 
	 * system where this HTTP client is running on. The working directory is
	 * used to store received files from the HTTP server.
	 */
	private String workingDirectory;
	
	/**
	 * Parse the HTTP response message from the server and store as a local
	 * file in the corresponding file tree.
	 * 
	 * @param 	inFromServer
	 * 			The input stream to parse the HTTP message from.
	 * @throws 	IOException
	 */
	public void parseHTTPMessage(InputStream inFromServer)
			throws IOException {
		// Construct a buffered input stream to wrap the input stream from the 
		// server to enable buffering and to use BufferedInputStream.mark() and
		// BufferedInputStream.reset() to return the input stream to a marked 
		// point in the stream.
		BufferedInputStream bis = new BufferedInputStream(inFromServer);
		
		// Set the current point in the stream as the return point with a 
		// maximum possible read data of 3072 bytes before the mark is given up.
		bis.mark(3072);
		
		// Wrap the buffered input stream with a InputStreamReader and wrap this
		// with a BufferedReader to create a buffered character stream for
		// reading the headers of the HTTP response message.
		InputStreamReader inStreamReader = new InputStreamReader(bis); 
		BufferedReader serverResponseText = new BufferedReader(inStreamReader);
		
		// String pathRequestedResource
		HTTPResponseMessage response = new HTTPResponseMessage();
		response.setPathRequestedResource(getRequestMessage().getLocalPathRequest());
		setResponseMessage(response);
		System.out.println("================RESPONSE================");
		responseMessage.setStatusLine(serverResponseText.readLine());
		System.out.println(responseMessage.getStatusLine());
		
		// parse message headers
		String responseString;
		while ((responseString = serverResponseText.readLine()) != null) {
			System.out.println(responseString);
			// check for the transition from the header part of the message to
			// the body part of the message.
			if (responseString.trim().equals("")) {
				break; // all headers are parsed
			}
			String[] header = responseString.split(":", 2);
			responseMessage.addAsHeader(header[0], header[1]);
		}
		
		// parse message body
		if (responseMessage.getResponseStatusCode() == 304) {
			System.out.println("[Notice] requested resource not modified");
		} else if (responseMessage.containsTextFile()) {
			System.out.println("[Notice] message body is text");
			parseTextMessageBody(serverResponseText);
		} else if (responseMessage.containsBinaryFile()) {
			System.out.println("[Notice] message body is binary data");
			bis.reset();
			parseBinaryMessageBody(bis);
		}
		
		if (getRequestMessage().getHTTPVersion() == "HTTP/1.0") {
			clientSocket.close();
		}
		System.out.println("");
	}

	/**
	 * Send the HTTP request message of this HTTP client to the host of this 
	 * client and return the response of the host to the send request message.
	 * 
	 * @return	The resulting input stream is the response from the host.
	 * @throws IOException
	 */
	public InputStream sendHTTPRequestMessage() throws IOException {
		HTTPRequestMessage httpRequest = getRequestMessage();
		// Create a socket to the given URI at the given port.
		if (getRequestMessage().getHTTPVersion() == "HTTP/1.0") {
			clientSocket = new Socket(host, port);
		}		

		// Create an output stream (convenient data writer) to this host.
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());

		// Create an input stream (convenient data reader) to this host and wrap
		// a buffered input stream around the input stream for increased 
		// efficiency.
		InputStream inFromServer = clientSocket.getInputStream();

		// Compose HTTP request message and send to the server.
		outToServer.writeBytes(httpRequest.composeMessage());
		System.out.println("message send: \n"
				+ httpRequest.composeMessage().trim() + "\nto " + host + ":"
				+ port);
		
		return inFromServer;
	}

	private void getEmbeddedObjects(String html) {
		// Use the Jsoup library to parse the html document.
		Document doc = Jsoup.parse(html, "http://" + host);

		// Select all images from the parsed html document and download relative
		// path embedded objects using a HTTP GET request message.
		Elements links = doc.select("img[src]");
		if (links.isEmpty()) {System.out.println("[Notice] no embedded images");}
		for (Element link : links) {
			try {
				URI uri = new URI(link.attr("abs:src"));
				if (uri.getHost().contentEquals(host)) {
					getRequestMessage().setMethod(HTTPMethod.GET);
					getRequestMessage().setLocalPathRequest(uri.getPath());
					InputStream inFromServer = sendHTTPRequestMessage();
					parseHTTPMessage(inFromServer);
				}
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	private void parseTextMessageBody(BufferedReader inFromServer)
			throws IOException {
		File file = new File(workingDirectory + "/" + host
				+ responseMessage.getPathRequestedResource());
		System.out.println(workingDirectory + "/" + host
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
	
	private void parseBinaryMessageBody(BufferedInputStream inStream)
			throws IOException {
		File file = new File(workingDirectory + "/" + host
				+ responseMessage.getPathRequestedResource());
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile());
		BufferedOutputStream outStream = new BufferedOutputStream(fos);
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		boolean endOfHeaderFound = false;
		int headerBytesRead;
		while ((bytesRead = inStream.read(buffer)) != -1) {
			headerBytesRead = 0;
			System.out.println("[Notice] " + bytesRead +" bytes read to buffer");
			if (!endOfHeaderFound) {
				String string = new String(buffer, 0, bytesRead);
				int indexOfEndOfHeader = string.indexOf("\r\n\r\n");
				if (indexOfEndOfHeader != -1) {
					headerBytesRead = indexOfEndOfHeader + 4;
					endOfHeaderFound = true;
				} else {
					bytesRead = 0;
				}
			}
			outStream.write(buffer, headerBytesRead, bytesRead-headerBytesRead);
			outStream.flush();
		}
		outStream.close();
		fos.close();
		System.out.println("End of stream");
	}

}