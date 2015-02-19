import java.io.*;
import java.net.*;

public class TCPServerSingleThreaded {
	public static void main(String argv[]) throws Exception {
		ServerSocket welcomeSocket = new ServerSocket(6789);
		while(true)	{
		Socket connectionSocket = welcomeSocket.accept();
		BufferedReader inFromClient = new BufferedReader(new
		InputStreamReader (connectionSocket.getInputStream()));
		DataOutputStream outToClient = new DataOutputStream
		(connectionSocket.getOutputStream());
		String clientSentence = inFromClient.readLine();
		System.out.println("Received: " + clientSentence);
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
			for (int j = 0; j < 100000000; j++) {
				double x = i/Math.sqrt(j);
				x = x/(x*Math.sqrt(x));
				for (int k = 0; k < 100000000; k++) {
					k = k*k*k*k;
				}
			}
		}
		String capsSentence = clientSentence.toUpperCase() + '\n';
		outToClient.writeBytes(capsSentence);
		}
	}
}
