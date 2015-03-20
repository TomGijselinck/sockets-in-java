package HTTP.client;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import HTTP.message.HTTPMethod;
import HTTP.message.HTTPRequestMessage;
import HTTP.message.HTTPResponseMessage;

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
		String clientName;
		if (args.length == 5) {
			clientName = args[4];
		} else {
			clientName = "omega";
		}
		
		// Create an instance of this class to enable bidirectional 
		// communication using HTTP.
		HTTPClient testClient = new HTTPClient(uri.getHost(), port, "/" + clientName);
		
		// Create the first request message using the given arguments.
		String requestUri = uri.getPath();
		if (requestUri.contentEquals("")) {
			requestUri = "/index.html";
		}
		HTTPRequestMessage request = new HTTPRequestMessage(method,
				requestUri, HTTPversion);
		request.addAsHeader("Host", testClient.getHost());
		request.addAsHeader("From", clientName + "@localhost");
		if (method == HTTPMethod.POST || method == HTTPMethod.PUT) {
			if (request.getLocalPathRequest().contains("index.html")) {
				request.setLocalPathRequest("/");
			}
			request.addAsHeader("Content-Type", "text/plain");
			try {
				InputStream in = System.in;
				InputStreamReader charsIn = new InputStreamReader(in);
				BufferedReader bufferedCharsIn = new BufferedReader(charsIn);
				String line = bufferedCharsIn.readLine();
				request.setMessageBody(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (method == HTTPMethod.GET || method == HTTPMethod.HEAD) {
			File file = new File(testClient.workingDirectory + "/"
					+ testClient.host + requestUri);
			if (file.exists()) {
				request.setIfModifiedSinceHeader(new Date(file.lastModified()));
			}
			if (HTTPversion.contains("1.1")) { //TODO require this header?
				request.addAsHeader("Connection", "Keep-Alive");
			}
		}
		testClient.setHTTPRequestMessage(request);
		
		// Send the constructed HTTP request message and create an input stream
		// for receiving the response message from the server.
		testClient.setClientSocket();
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
	
	/**
	 * Initialize this new HTTP client with the given host, given port and given 
	 * client name.
	 * 
	 * @param 	host
	 * 			The host for this new HTTP client.
	 * @param 	port
	 * 			The port for this new HTTP client.
	 * @param 	clientName
	 * 			The client name for this new HTTP client.
	 */
	public HTTPClient(String host, int port, String clientName) {
		setHost(host);
		setPort(port);
		setWorkingDirectory("/home/tom/http" + clientName);
	}

	/**
	 * Initialize this new HTTP client with the given host, given port and with
	 * a client name of Lambda.
	 * 
	 * @param 	host
	 * 			The host for this new HTTP client.
	 * @param 	port
	 * 			The port for this new HTTP client.
	 */
	public HTTPClient(String host, int port) {
		this(host, port, "Lambda");
	}

	/**
	 * Return the request message of this HTTP client.
	 */
	public HTTPRequestMessage getRequestMessage() {
		return requestMessage;
	}

	/**
	 * Set the request message for this HTTP client to the given request 
	 * message.
	 * 
	 * @param 	requestMessage
	 * 			The new request message for this HTTP client.
	 */
	public void setHTTPRequestMessage(HTTPRequestMessage requestMessage) {
		this.requestMessage = requestMessage;
		requestMessage.setClient(this);
	}

	/**
	 * Variable referencing the request message of this HTTP client.
	 */
	private HTTPRequestMessage requestMessage;

	/**
	 * Return the response message of this HTTP client.
	 */
	public HTTPResponseMessage getResponseMessage() {
		return responseMessage;
	}

	/**
	 * Set the response message for this HTTP client to the given response
	 * message.
	 * 
	 * @param 	responseMessage
	 * 			The new response message for this HTTP client.
	 */
	public void setResponseMessage(HTTPResponseMessage responseMessage) {
		this.responseMessage = responseMessage;
	}

	/**
	 * Variable referencing the response message of this HTTP client. 
	 */
	private HTTPResponseMessage responseMessage;

	/**
	 * Return the host of this HTTP client.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Set the host of this HTTP client to the given host.
	 * 
	 * @param 	host
	 * 			The new host for his HTTP client.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Variable referencing the host of this HTTP client.
	 */
	private String host;

	/**
	 * Return the port of this HTTP client.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set the port for this HTTP client to the given port.
	 * 
	 * @param 	port
	 * 			The new port for this HTTP client.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Variable referencing the port of this HTTP client.
	 */
	private int port;
	
	/**
	 * Return the socket of this HTTP client.
	 */
	public Socket getClientSocket() {
		return clientSocket;
	}
	
	/**
	 * Set the socket for this HTTP client by creating a new socket using the
	 * host of this HTTP client and the port of this HTTP client.
	 * 
	 * @throws 	UnknownHostException
	 * @throws 	IOException
	 */
	public void setClientSocket() throws UnknownHostException, IOException {
		clientSocket = new Socket(getHost(), getPort());
	}
	
	/**
	 * Set the socket for this HTTP client to the given socket.
	 * 
	 * @param 	socket
	 * 			The new socket for this HTTP client.
	 */
	public void setClientSocket(Socket socket) {
		clientSocket = socket;
	}
	
	/**
	 * Variable referencing the socket of this HTTP client.
	 */
	private Socket clientSocket;

	/**
	 * Return the working directory of this HTTP client.
	 * 		The working directory is the directory where the server serves its
	 * 		content.
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}
	
	/**
	 * Set the working directory for this HTTP client to the given directory.
	 * 
	 * @param 	directory
	 * 			The new working directory for this HTTP client.
	 */
	public void setWorkingDirectory(String directory) {
		workingDirectory = directory;
	}
	
	/**
	 * Variable referencing the working directory in the file system of the 
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
	 * @throws 	InterruptedException 
	 */
	public void parseHTTPMessage(InputStream inFromServer)
			throws IOException, InterruptedException {
		// Construct a buffered input stream to wrap the input stream from the 
		// server to enable buffering and to use BufferedInputStream.mark() and
		// BufferedInputStream.reset() to return the input stream to a marked 
		// point in the stream.
		BufferedInputStream bis = new BufferedInputStream(inFromServer);
		
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
		
		// Set the current point in the stream as the return point with a 
		// maximum possible read data of 10240 bytes before the mark is given up.
		bis.mark(1024*1024); // to be sure mark point is not lost
		String statusLine = serverResponseText.readLine();
		getResponseMessage().setStatusLine(statusLine);
		System.out.println(getResponseMessage().getStatusLine());
		
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
			getResponseMessage().addAsHeader(header[0], header[1]);
		}
		
		// parse message body if it exists
		HTTPMethod method = getRequestMessage().getMethod();
		if (method == HTTPMethod.HEAD) {
			// no message body, do nothing
		} else if (method == HTTPMethod.POST || method == HTTPMethod.PUT) {
			// no message body, do nothing
		} else if (getResponseMessage().getResponseStatusCode() == 400) {
			// bad request, no message body, do nothing
		} else if (getResponseMessage().getResponseStatusCode() == 404) {
			// Not found, no message body, do nothing
		} else if (getResponseMessage().getResponseStatusCode() == 304) {
			System.out.println("[Notice] requested resource not modified");
		} else if (getResponseMessage().containsTextFile()) {
			System.out.println("[Notice] message body is text");
			bis.reset();
			parseBodyMessage(bis);
		} else if (getResponseMessage().containsBinaryFile()) {
			System.out.println("[Notice] message body is binary data");
			bis.reset();
			parseBodyMessage(bis);
		}
		
		if (getRequestMessage().isHTTP1_0()) {
			getClientSocket().close();
		}
		System.out.println("");
	}

	/**
	 * Send the HTTP request message of this HTTP client to the host of this 
	 * client and return the response of the host to the send request message.
	 * 
	 * @return	InputStream
	 * 			The resulting input stream as the response from the host.
	 * @throws 	IOException
	 */
	public InputStream sendHTTPRequestMessage() throws IOException {
		HTTPRequestMessage httpRequest = getRequestMessage();
		// Create a socket to the given URI at the given port.
		if (getRequestMessage().isHTTP1_0()) {
			setClientSocket(new Socket(getHost(), getPort()));
		}

		// Create an output stream (convenient data writer) to this host.
		DataOutputStream outToServer = new DataOutputStream(
				getClientSocket().getOutputStream());

		// Create an input stream (convenient data reader) to this host and wrap
		// a buffered input stream around the input stream for increased 
		// efficiency.
		InputStream inFromServer = getClientSocket().getInputStream();

		// Compose HTTP request message and send to the server.
		outToServer.writeBytes(httpRequest.composeMessage());
		outToServer.flush();
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
		int numberOfObjectsRequested = 0;
		for (Element link : links) {
			try {
				URI uri = new URI(link.attr("abs:src"));
				if (uri.getHost().contentEquals(host)) {
					getRequestMessage().setMethod(HTTPMethod.GET);
					getRequestMessage().setLocalPathRequest(uri.getPath());
					File file = new File(getWorkingDirectory() + "/" + getHost()
							+ getRequestMessage().getLocalPathRequest());
					if (file.exists()) {
						getRequestMessage().setIfModifiedSinceHeader(new Date(file.lastModified()));
					} else {
						getRequestMessage().removeAsHeader("If-Modified-Since");
					}
					// Add "Connection: close" as header to indicate last 
					// request message.
					if (numberOfObjectsRequested +1 == links.size() && getRequestMessage().isHTTP1_1()) {
						getRequestMessage().addAsHeader("Connection", "close");
					}
					InputStream inFromServer = sendHTTPRequestMessage();
					parseHTTPMessage(inFromServer);
					numberOfObjectsRequested += 1;
				}
			} catch (URISyntaxException | IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	private void parseBodyMessage(BufferedInputStream inStream) throws IOException {
		File file = new File(getWorkingDirectory() + "/" + getHost()
				+ getResponseMessage().getPathRequestedResource());
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile());
		BufferedOutputStream outStream = new BufferedOutputStream(fos);
		boolean text = getResponseMessage().containsTextFile();
		byte[] buffer = new byte[1024];
		boolean endOfHeaderFound = false;
		int bytesToRead = Integer.parseInt(getResponseMessage().getHeaderValue(
				"Content-Length"));
		int headerBytesRead;
		int bodyBytesRead = 0;
		int bytesRead;
		while (bodyBytesRead < bytesToRead) {
			bytesRead = inStream.read(buffer);
			headerBytesRead = 0;
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
			if (text) {
				// Only display the part of the buffer that is actually written.
				byte [] subArray = Arrays.copyOfRange(buffer, headerBytesRead, bytesRead);
				String textToDisplay = new String(subArray);
				System.out.println(textToDisplay);
				getResponseMessage().addToMessageBody(textToDisplay);
			} else {
				System.out.println("[Notice] " + bytesRead +" bytes read from buffer");
			}
			bodyBytesRead += bytesRead - headerBytesRead;
			outStream.write(buffer, headerBytesRead, bytesRead-headerBytesRead);
			outStream.flush();
		}
		outStream.close();
		fos.close();
		System.out.println("[Notice] end of stream");
	}

}