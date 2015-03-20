package HTTP.server;
import java.net.*;
import java.util.HashSet;
import java.util.Set;


public class HTTPServer {
	private static ServerSocket welcomeSocket;

	public static void main(String argv[]) throws Exception {
		// Create a server (incoming) socket on port 5678.
		welcomeSocket = new ServerSocket();
		welcomeSocket.setReuseAddress(true);
		welcomeSocket.bind(new InetSocketAddress(5678));
		
		// Create list with all connected client sockets.
		Set<Integer> connectedSockets = new HashSet<Integer>();
		
		// Wait for a connection to be made to the server socket.
		while(true) {
			// Create a 'real' socket from the Server socket.
			Socket connectionSocket = welcomeSocket.accept();
			int hash = connectionSocket.hashCode();
			
			// If a connection is made with a client, create a new thread to
			// resolve the client request.
			if (connectionSocket != null
					&& !connectedSockets.contains(hash)) {
				connectedSockets.add(connectionSocket.hashCode());
				System.out.println(hash + " New handler");
				Handler h = new Handler(connectionSocket, hash);
				Thread thread = new Thread(h);
				thread.start();
			} else if (connectedSockets.contains(hash)) {
				System.out.println("Already connected to " + hash);
			}
		}
	}
}