import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class Handler implements Runnable {
	Socket socket;
	
	public Handler(Socket connectionSocket) {
		socket = connectionSocket;
	}
	
	@Override
	public void run() {
		BufferedReader inFromClient;
		try {
			// Create inputstream (convenient data reader) to this host.
			InputStreamReader inputStreamReader = 
					new InputStreamReader(socket.getInputStream());
			inFromClient = new BufferedReader(inputStreamReader);
			
			// Create outputstream (convenient data writer) to this host.
			DataOutputStream outToClient = new DataOutputStream(
					socket.getOutputStream());
			
			// Read text from the client, make it uppercase and write it back.
			String clientSentence = inFromClient.readLine();
			System.out.println("RECIEVED: " + clientSentence);
			String capsSentence = clientSentence.toUpperCase() + '\n';
			outToClient.writeBytes(capsSentence);
			System.out.println("SENT: "+ capsSentence);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
