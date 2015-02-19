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
//
//	public void CalRequest(Socket socket) {
//		this.socket = socket;
//	}
	
	@Override
	public void run() {
		BufferedReader inFromClient;
		try {
			inFromClient = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(
					socket.getOutputStream());
			String clientSentence = inFromClient.readLine();
			System.out.println("RECIEVED: " + clientSentence);
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
