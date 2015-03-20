package HTTP.server;
import java.io.*;
import java.net.Socket;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import HTTP.message.HTTPMethod;
import HTTP.message.HTTPRequestMessage;
import HTTP.message.HTTPResponseMessage;


public class Handler implements Runnable {
	//tmp
	int hash;
	
	/**
	 * Initialize this new HTTP server handler with the given socket and given
	 * hash.
	 * 
	 * @param 	connectionSocket
	 * 			The socket for this new handler.
	 * @param 	hash
	 * 			The hash for this new handler.
	 */
	public Handler(Socket connectionSocket, int hash) {
		socket = connectionSocket;
		requestMessage = new HTTPRequestMessage();
		this.hash = hash;
		requestAccepted = false;
	}
	
	@Override
	public void run() {
		BufferedReader inFromClient;
		boolean threadUsed = false;
		int timer = 0;
		int DELAY = 10;
		int TIMEOUT = 2000;
		try {
			while (!socket.isClosed()){
				// Create an input stream (convenient data reader) to this host.
				InputStreamReader inputStreamReader = 
						new InputStreamReader(getSocket().getInputStream());
				inFromClient = new BufferedReader(inputStreamReader);	
				requestAccepted = parseRequestMessage(inFromClient);
				
				// If the request message from the client is accepted, create
				// a response message and send it back to the client.
				if (requestAccepted) {
					threadUsed = true;
					timer = 0;
					HTTPResponseMessage response = new HTTPResponseMessage();
					response.setDate(new Date());
					HTTPMethod method = getRequestMessage().getMethod();
					String HTTPVersion = getRequestMessage().getHTTPVersion();
					
					// Add "Connection: close" header if the client has included
					// this header in its request to this server.
					if (getRequestMessage().hasAsHeaderValue("Connection", "close")) {
						response.addAsHeader("Connection", "close");
					}
					
					// Send a "400 Bad Request" and close the connection to the 
					// client connected to this server thread if the client used 
					// HTTP 1.1 and has not included the host header in its 
					// request message.
					if (getRequestMessage().isHTTP1_1()
							&& !getRequestMessage().hasAsHeader("Host")) {
						response.setStatusLine(HTTPVersion + " 400 Bad Request");
						setResponseMessage(response);
						sendResponseMessage();
						System.out.println("[Notice] bad request --> closing connection");
						getSocket().close();
						break;
					}
					
					// Resolve different HTTP methods; HEAD, GET, PUT, POST
					// Resolve POST and PUT method
					if (method == HTTPMethod.POST || method == HTTPMethod.PUT) {
						boolean append = false;
						String fileName = FilenameUtils.getName(getLocalPathRequest());
						String fullFileName = "";
						if (fileName.contentEquals("") || fileName.contains("index.html")) {
							String fileDirectory = FilenameUtils.getFullPath(getLocalPathRequest());
							if (method == HTTPMethod.POST) {
								fullFileName = serverDirectory + fileDirectory + "POST.txt";
								append = true;
							} else if (method == HTTPMethod.PUT) {
								fullFileName = serverDirectory + fileDirectory + "PUT.txt";
								append = false;
							}
						} else {
							fullFileName = serverDirectory + getLocalPathRequest();
							if (method == HTTPMethod.POST) {
								append = true;
							} else if (method == HTTPMethod.PUT) {
								append = false;
							}
						}
						FileWriter fw = new FileWriter(fullFileName, append);
						fw.write(getRequestMessage().getMessageBody());
						fw.close();
						response.setStatusLine(HTTPVersion + " 200 OK");
						setResponseMessage(response);
						sendResponseMessage();
						
					// Resolve HEAD method
					} else if (method == HTTPMethod.HEAD) {
						File file = new File(serverDirectory + getLocalPathRequest());
						if (file.exists()) {
							response.setStatusLine(HTTPVersion + " 200 OK");
							Date fileDate = new Date(file.lastModified());
							response.setLastModifiedHeader(fileDate);
							response.setContentType(getLocalPathRequest());
						} else {
							response.setStatusLine(HTTPVersion + " 404 Not found");
						}
						setResponseMessage(response);
						sendResponseMessage();
						
					//Resolve GET method
					} else if (method == HTTPMethod.GET) {
						File file = new File(serverDirectory + getLocalPathRequest());
						if (file.exists()) {
							if (getRequestMessage().hasAsHeader("If-Modified-Since")) {
								Date fileDate = new Date(file.lastModified());
								Date ifModifiedSinceDate = getRequestMessage().getIfModifiedSinceDate();
								if (ifModifiedSinceDate.after(fileDate)) {
									response.setStatusLine(HTTPVersion + " 304 Not Modified");
									response.setLastModifiedHeader(fileDate);
								} else {
									response.setStatusLine(HTTPVersion + " 200 OK");
								}							
							} else {
								response.setStatusLine(HTTPVersion + " 200 OK");
							}
							if (getRequestMessage().isHTTP1_1() && !response.hasAsHeaderValue("Connection", "close")) {
								response.addAsHeader("Connection", "Keep-Alive");
							}
							response.setContentType(getLocalPathRequest());
							response.addAsHeader("Content-Length", String.valueOf(file.length()));
							setResponseMessage(response);
							sendResponseMessage();
							if (responseMessage.getResponseStatusCode() == 200) {
								BufferedInputStream fileStream = new BufferedInputStream(getFileStream(file));
								sendFile(fileStream);
							}
						} else {
							response.setStatusLine(HTTPVersion + " 404 Not found");
							response.addAsHeader("Connection", "close");
							setResponseMessage(response);
							sendResponseMessage();
						}
					
					// No Correct method used, send bad request and close the 
					// socket of this server handler.
					} else {
						response.setStatusLine(HTTPVersion + " 400 Bad Request");
						setResponseMessage(response);
						sendResponseMessage();
						System.out.println("[Notice] bad request --> closing connection");
						getSocket().close();
						break;
					}
					
					// Close the socket of this server handler if HTTP 1.0 is
					// used or if the response message contains the
					// "Connection: close" header.
					if (getRequestMessage().isHTTP1_0() || getResponseMessage().hasAsHeaderValue(
									"Connection", "close")) {
						System.out.println(hash + " --> Closing this connection");
						getSocket().close();
					}
				
				// Use a timeout to limit the maximum time an idle connection is
				// allowed to remain connected.
				} else {
					try {
						Thread.sleep(DELAY);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					timer += DELAY;
					if (timer >= TIMEOUT) {
						if (!threadUsed) {
							// this thread is never used
							getSocket().close();
						} else {
							System.out.println("[Notice] timeout occured");
							System.out.println(hash + " --> Closing this connection");
							getSocket().close();							
						}
					}
				}
			}
		// Catch any possible exception and try to send a "500 Server Error" to
		// the client.
		} catch (Exception e) {
			e.printStackTrace();
			try {
				responseMessage.setStatusLine(getRequestMessage().getHTTPVersion() + " 500 Server Error");
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
	public HTTPRequestMessage getRequestMessage() {
		return requestMessage;
	}
	
	/**
	 * Set the HTTPRequestMessage of this handler to the given request message.
	 * 
	 * @param 	request
	 * 			The new request message for this handler.
	 */
	public void setRequestMesssage(HTTPRequestMessage request) {
		requestMessage = request;
	}
	
	/**
	 * Variable referencing the HTTP request message of this HTTP server 
	 * handler.
	 */
	private HTTPRequestMessage requestMessage;
	
	/**
	 * Return the HTTP response message of this handler.
	 */
	public HTTPResponseMessage getResponseMessage() {
		return responseMessage;
	}
	
	/**
	 * Set the HTTP response message of this handler to the given response 
	 * message.
	 * 
	 * @param 	responseMessage
	 * 			The new response message for this handler.
	 */
	public void setResponseMessage(HTTPResponseMessage responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	/**
	 * Variable referencing the HTTP response message of this HTTP server 
	 * handler.
	 */
	private HTTPResponseMessage responseMessage;
	
	/**
	 * Variable referencing if the last request message from the client 
	 * connected to this server is accepted or not.
	 */
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
	 * @throws 	IOException 
	 */
	private boolean parseRequestMessage(BufferedReader inFromClient) throws IOException {
		setRequestMesssage(new HTTPRequestMessage());
		String requestLine = inFromClient.readLine();
		boolean requestAccept = false;
		if (requestLine == null) {
			requestAccept = false;
		} else {
			System.out.println(hash + " Request line: " + requestLine);
			requestAccept = true;
			getRequestMessage().setRequestLine(requestLine);
			System.out.println(hash + " Message:\r\n" + getRequestMessage().getRequestLine());
			
			// parse message headers
			String responseString;
			boolean bodyMessage = false;
			boolean headerMessage = true;
			while (inFromClient.ready()) {
				responseString = inFromClient.readLine();
				System.out.println(responseString);
				// check for the transition from the header part of the message to
				// the body part of the message.
				if (responseString.trim().equals("")) {
					// all headers are parsed
					headerMessage = false;
					bodyMessage = true;
					continue;
				}
				if (headerMessage) {
					String[] header = responseString.split(":", 2);
					getRequestMessage().addAsHeader(header[0], header[1]);					
				} else if (bodyMessage) {
					getRequestMessage().addToMessageBody(responseString + "\n");
				}
			}
		}
		return requestAccept;
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
				getSocket().getOutputStream());
		outToClient.writeBytes(getResponseMessage().composeMessage());
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
	
	private String getLocalPathRequest() {
		String path = getRequestMessage().getLocalPathRequest();
		if (path.contentEquals("/")) {
			path = "/index.html";
		}
		return path;
	}
	
	

}
