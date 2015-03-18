import java.io.*;
import java.net.Socket;
import java.util.Date;


public class Handler implements Runnable {
	//tmp
	int hash;
	
	public Handler(Socket connectionSocket, int hash) {
		socket = connectionSocket;
		requestMessage = new HTTPRequestMessage();
		this.hash = hash;
		requestAccepted = false;
	}
	
	@Override
	public void run() {
		BufferedReader inFromClient;
		try {
			while (!socket.isClosed()){
				System.out.println(hash + " Start new run");
				// Create inputstream (convenient data reader) to this host.
				InputStreamReader inputStreamReader = 
						new InputStreamReader(socket.getInputStream());
				inFromClient = new BufferedReader(inputStreamReader);	
				
				parseRequestMessage(inFromClient);
				if (requestAccepted) {			
					HTTPResponseMessage response = new HTTPResponseMessage();
					response.setDate(new Date());
					HTTPMethod method = getHTTPRequestMessage().getMethod();
					if (method == HTTPMethod.POST) {
						//do something
					} else if (method == HTTPMethod.PUT) {
						//do something else
					} else if (method == HTTPMethod.HEAD) {
						File file = new File(serverDirectory + getHTTPRequestMessage().getLocalPathRequest());
						if (file.exists()) {
							response.setStatusLine(requestMessage.getHTTPVersion() + " 200 OK");
						} else {
							response.setStatusLine(requestMessage.getHTTPVersion() + " 404 Not found");
						}
						setHTTPResponseMessage(response);
						sendResponseMessage();
					} else if (method == HTTPMethod.GET) {
						File file = new File(serverDirectory + getHTTPRequestMessage().getLocalPathRequest());
						if (file.exists()) {
							if (requestMessage.hasAsHeader("If-Modified-Since")) {
								Date fileDate = new Date(file.lastModified());
								Date ifModifiedSinceDate = requestMessage.getIfModifiedSinceDate();
								if (ifModifiedSinceDate.after(fileDate)) {
									response.setStatusLine(requestMessage.getHTTPVersion() + " 304 Not Modified");
									response.setLastModifiedHeader(fileDate);
								} else {
									response.setStatusLine(requestMessage.getHTTPVersion() + " 200 OK");
								}							
							} else {
								response.setStatusLine(requestMessage.getHTTPVersion() + " 200 OK");
							}
							if (requestMessage.isHTTP1_1()) {
								response.addAsHeader("Connection", "Keep-Alive");
							}
							response.setContentType(requestMessage.getLocalPathRequest());
							response.addAsHeader("Content-Length", String.valueOf(file.length()));
							setHTTPResponseMessage(response);
							sendResponseMessage();
							if (responseMessage.getResponseStatusCode() == 200) {
								BufferedInputStream fileStream = new BufferedInputStream(getFileStream(file));
								sendFile(fileStream);
							}
						} else {
							response.setStatusLine(requestMessage.getHTTPVersion() + " 404 Not found");
							setHTTPResponseMessage(response);
							sendResponseMessage();
						}
					}
					if (requestMessage.isHTTP1_0()) {
						socket.close();
					}
				}
//				Thread.sleep(100); //TODO adjust + set timeout
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				responseMessage.setStatusLine(requestMessage.getHTTPVersion() + " 500 Server Error");
				sendResponseMessage();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Return the the socket of this handler.
	 */
	public Socket getSocket() {
		return socket;
	}
	
	/**
	 * Variable referencing the socket of this HTTP server handler.
	 */
	private Socket socket;
	
	/**
	 * Return the HTTP request message of this handler.
	 */
	public HTTPRequestMessage getHTTPRequestMessage() {
		return requestMessage;
	}
	
	/**
	 * Variable referencing the HTTP request message of this HTTP server 
	 * handler.
	 */
	private HTTPRequestMessage requestMessage;
	
	/**
	 * Return the HTTP response message of this handler.
	 */
	public HTTPResponseMessage getHTTPResponseMessage() {
		return responseMessage;
	}
	
	/**
	 * Set the HTTP response message of this handler to the given response 
	 * message.
	 * 
	 * @param 	responseMessage
	 * 			The new response message for this handler.
	 */
	public void setHTTPResponseMessage(HTTPResponseMessage responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	/**
	 * Variable referencing the HTTP response message of this HTTP server 
	 * handler.
	 */
	private HTTPResponseMessage responseMessage;
	
	private boolean requestAccepted;
	
	/**
	 * Variable registering the directory of the web page that this server 
	 * serves.
	 */
	private static final String serverDirectory = "/home/tom/http/server/"; 
	
	/**
	 * Parse the HTTP request message from the client.
	 * 
	 * @param 	inFromClient
	 * 			The buffered reader to read the HTTP request message from.
	 * @throws IOException 
	 */
	private void parseRequestMessage(BufferedReader inFromClient) throws IOException {
		requestMessage = new HTTPRequestMessage();
		String requestLine = inFromClient.readLine();
		System.out.println(hash + " Request line: " + requestLine);
		if (requestLine == null) {
			requestAccepted = false;
			socket.close();
			System.out.println(hash + " --> Closing this connection");
		} else {
			requestAccepted = true;
			requestMessage.setRequestLine(requestLine);
			System.out.println(hash + " Message:\r\n" + requestMessage.getRequestLine());
			
			// parse message headers
			String responseString;
			while ((responseString = inFromClient.readLine()) != null) {
				System.out.println(responseString);
				// check for the transition from the header part of the message to
				// the body part of the message.
				if (responseString.trim().equals("")) {
					break; // all headers are parsed
				}
				String[] header = responseString.split(":", 2);
				requestMessage.addAsHeader(header[0], header[1]);
			}
		}
	}
	
	/**
	 * Send the HTTP response message of this HTTP server handler to the client
	 * associated with the socket of this HTTP server handler.
	 * 
	 * @throws IOException 
	 */
	private void sendResponseMessage() throws IOException {
		// Create outputstream (convenient data writer) to this host.
		DataOutputStream outToClient = new DataOutputStream(
				socket.getOutputStream());
		outToClient.writeBytes(getHTTPResponseMessage().composeMessage());
		outToClient.flush();
	}
	
	private BufferedInputStream getFileStream(File file) {
		if (!file.exists() || !file.canRead()) {
			System.out.println("Can't read " + file);
			return null;
		}
		
		try {
			InputStream is = new DataInputStream(new FileInputStream(file));
			BufferedInputStream inFromFile = new BufferedInputStream(is);
			return inFromFile;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void sendFile(BufferedInputStream fileStream) throws IOException {
		// Create an output stream (convenient data writer) to the client.
		DataOutputStream dos = new DataOutputStream(
				getSocket().getOutputStream());
		BufferedOutputStream outToClient = new BufferedOutputStream(dos);
		
		int bytesRead;
		byte[] buffer = new byte[1024];
		while ((bytesRead = fileStream.read(buffer)) != -1) {
			outToClient.write(buffer);
			buffer = new byte[1024];
			System.out.println("[Notice] " + bytesRead +" bytes written to buffer");
		}
		outToClient.flush();
	}
	
	

}
