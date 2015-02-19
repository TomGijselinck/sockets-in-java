import java.net.*;

public class TCPServer {
	private static ServerSocket welcomeSocket;

	public static void main(String argv[]) throws Exception {
		welcomeSocket = new ServerSocket(6789);
		while(true) {
			Socket connectionSocket = welcomeSocket.accept();
			if(connectionSocket != null) {
				Handler h = new Handler(connectionSocket);
				Thread thread = new Thread(h);
				thread.start();
			}
		}
	}
}
