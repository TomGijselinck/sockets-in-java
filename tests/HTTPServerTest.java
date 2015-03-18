package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import HTTP.client.HTTPClient;
import HTTP.server.HTTPServer;

public class HTTPServerTest {

	@Test
	public void main_test() throws Exception {
		// run server
		HTTPServer.main(new String[] {});
		System.out.println("server started");
		
		// run client
		HTTPClient.main(new String[] {"GET", "localhost", "5678", "HTTP/1.1"});
		System.out.println("client started");
	}

}
