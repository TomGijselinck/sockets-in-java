import java.io.*;
import java.net.*;

public class TCPClient {
	
	public static void main(String[] args)  throws Exception {
		// Create a buffered reader to take user input from the console.
		BufferedReader inFromUser = new BufferedReader(
				new InputStreamReader(System.in));
		
		// Create a socket to localhost (this machine, port 6789).
		Socket clientSocket = new Socket("localhost", 6789);
		
		// Create outputstream (convenient data writer) to this host.
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		
		// Create an inputstream (convenient data reader) to this host.
		BufferedReader inFromServer = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));
		
		// Read text from the console and write it to the server.
		String sentence = inFromUser.readLine();
		outToServer.writeBytes(sentence + '\n');
		
		// Read text from the server and write it to the console.
		String modifiedSentence = inFromServer.readLine();		
		System.out.println("FROM SERVER: " + modifiedSentence);
		
		clientSocket.close();
	}

}