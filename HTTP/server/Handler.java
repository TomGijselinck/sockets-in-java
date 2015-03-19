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
	
	public Handler(Socket connectionSocket, int hash) {
		socket = connectionSocket;
		requestMessage = new HTTPRequestMessage();
		this.hash = hash;
		requestAccepted = false;
	}
	
	@Override
	public void run() {
		BufferedReader inFromClient;
		int timer = 0;
		int DELAY = 10;
		int TIMEOUT = 2000; // 10 seconds
		try {
			while (!socket.isClosed()){
				// Create inputstream (convenient data reader) to this host.
				InputStreamReader inputStreamReader = 
						new InputStreamReader(getSocket().getInputStream());
				inFromClient = new BufferedReader(inputStreamReader);	
				
				parseRequestMessage(inFromClient);
				if (requestAccepted) {
					timer = 0;
					HTTPResponseMessage response = new HTTPResponseMessage();
					response.setDate(new Date());
					HTTPMethod method = getHTTPRequestMessage().getMethod();
					if (getHTTPRequestMessage().hasAsHeaderValue("Connection", "close")) {
						response.addAsHeader("Connection", "close");
					}
					
					// Resolve different HTTP methods; HEAD, GET, PUT, POST
					if (method == HTTPMethod.POST) {
						String fileName = FilenameUtils.getName(getLocalPathRequest());
						String fullFileName;
						if (fileName.contentEquals("") || fileName.contains("index.html")) {
							String fileDirectory = FilenameUtils.getFullPath(getLocalPathRequest());
							fullFileName = serverDirectory + fileDirectory + "POST.txt";
							System.out.println(fullFileName);
						} else {
							fullFileName = serverDirectory + getLocalPathRequest();
							System.out.println(fullFileName);
						}
						FileWriter fw = new FileWriter(fullFileName, true);
						fw.write(getHTTPRequestMessage().getMessageBody());
						fw.close();
						response.setStatusLine(getHTTPRequestMessage().getHTTPVersion() + " 200 OK");
						setHTTPResponseMessage(response);
						sendResponseMessage();
					} else if (method == HTTPMethod.PUT) {
						//do something else
					} else if (method == HTTPMethod.HEAD) {
						File file = new File(serverDirectory + getLocalPathRequest());
						if (file.exists()) {
							response.setStatusLine(getHTTPRequestMessage().getHTTPVersion() + " 200 OK");
							Date fileDate = new Date(file.lastModified());
							response.setLastModifiedHeader(fileDate);
						} else {
							response.setStatusLine(getHTTPRequestMessage().getHTTPVersion() + " 404 Not found");
						}
						setHTTPResponseMessage(response);
						sendResponseMessage();
					} else if (method == HTTPMethod.GET) {
						File file = new File(serverDirectory + getLocalPathRequest());
						if (file.exists()) {
							if (getHTTPRequestMessage().hasAsHeader("If-Modified-Since")) {
								Date fileDate = new Date(file.lastModified());
								Date ifModifiedSinceDate = getHTTPRequestMessage().getIfModifiedSinceDate();
								if (ifModifiedSinceDate.after(fileDate)) {
									response.setStatusLine(getHTTPRequestMessage().getHTTPVersion() + " 304 Not Modified");
									response.setLastModifiedHeader(fileDate);
								} else {
									response.setStatusLine(getHTTPRequestMessage().getHTTPVersion() + " 200 OK");
								}							
							} else {
								response.setStatusLine(getHTTPRequestMessage().getHTTPVersion() + " 200 OK");
							}
							if (getHTTPRequestMessage().isHTTP1_1()) {
								response.addAsHeader("Connection", "Keep-Alive");
							}
							response.setContentType(getLocalPathRequest());
							response.addAsHeader("Content-Length", String.valueOf(file.length()));
							setHTTPResponseMessage(response);
							sendResponseMessage();
							if (responseMessage.getResponseStatusCode() == 200) {
								BufferedInputStream fileStream = new BufferedInputStream(getFileStream(file));
								sendFile(fileStream);
							}
						} else {
							response.setStatusLine(getHTTPRequestMessage().getHTTPVersion() + " 404 Not found");
							setHTTPResponseMessage(response);
							sendResponseMessage();
						}
					}
					if (getHTTPRequestMessage().isHTTP1_0() || getHTTPRequestMessage().hasAsHeaderValue(
									"Connection", "close")) {
						getSocket().close();
					}
				} else {
					try {
						Thread.sleep(DELAY);
					} catch (InterruptedException ie) {}
					timer += DELAY;
					if (timer >= TIMEOUT) {
						System.out.println("[Notice] timeout occured");
						System.out.println(hash + " --> Closing this connection");
						getSocket().close();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				responseMessage.setStatusLine(getHTTPRequestMessage().getHTTPVersion() + " 500 Server Error");
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
	
	public void setHTTPRequestMesssage(HTTPRequestMessage request) {
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
		setHTTPRequestMesssage(new HTTPRequestMessage());
		String requestLine = inFromClient.readLine();
		if (requestLine == null) {
			requestAccepted = false;
		} else {
			System.out.println(hash + " Request line: " + requestLine);
			requestAccepted = true;
			requestMessage.setRequestLine(requestLine);
			System.out.println(hash + " Message:\r\n" + requestMessage.getRequestLine());
			
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
					requestMessage.addAsHeader(header[0], header[1]);					
				} else if (bodyMessage) {
					requestMessage.addToMessageBody(responseString + "\n");
				}
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
				getSocket().getOutputStream());
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
	
	private String getLocalPathRequest() {
		String path = getHTTPRequestMessage().getLocalPathRequest();
		if (path.contentEquals("/")) {
			path = "/index.html";
		}
		return path;
	}
	
	

}
