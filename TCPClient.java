import java.io.*;
import java.net.*;

public class TCPClient {
	
	public static void main(String[] args)  throws Exception {
		int port = 80;
		try {
			port = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.err.println("Argument" + args[2] + " must be an integer.");
	        System.exit(1);
		}
		String host = args[1];
		PrintWriter outToServer = null;
		BufferedReader inFromServer = null;
		
		
		// Create a socket to the given URI at the given port (default port 80).
		Socket clientSocket = new Socket(client.getURI(), client.getPort());
		
		// Create outputstream (convenient data writer) to this host.
		outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		
		// Create an inputstream (convenient data reader) to this host.
		inFromServer = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));
		
		// Compose HTTP message and send to server.
		String message = "HEADER / HTTP/1.0";
		outToServer.writeBytes(message + '\n');
		
		// Read text from the server and write it to the console.
		String response = inFromServer.readLine();		
		System.out.println("FROM SERVER: " + response);
		
		clientSocket.close();
	}

}