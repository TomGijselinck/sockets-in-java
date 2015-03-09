import java.net.*;

public class HTTPServer {
	private static ServerSocket welcomeSocket;

	public static void main(String argv[]) throws Exception {
		// Create server (incoming) socket on port 6789.
		welcomeSocket = new ServerSocket(6789);
		
		// Wait for a connection to be made to the server socket.
		while(true) {
			// Create a 'real' socket from the Server socket.
			Socket connectionSocket = welcomeSocket.accept();
			
			// If a connection is made with a client, create a new thread to
			// resolve the client request.
			if(connectionSocket != null) {
				Handler h = new Handler(connectionSocket);
				Thread thread = new Thread(h);
				thread.start();
			}
		}
	}
}