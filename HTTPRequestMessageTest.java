import static org.junit.Assert.*;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

public class HTTPRequestMessageTest {

	/**
	 * Variable referencing a HTTP request message with a GET method.
	 */
	private static HTTPRequestMessage methodGet;

	/**
	 * Set up an immutable test fixture.
	 */
	@BeforeClass
	public static void setUpImmutableFixture() {
		methodGet = new HTTPRequestMessage(HTTPMethod.GET, "/index.html",
				"HTTP/1.0");
	}

	@Test
	public void setIfModifiedSinceHeader_Test() {
		methodGet.setIfModifiedSinceHeader(new Date());
		assertNotNull(methodGet.getHeaderValue("If-Modified-Since"));
		System.out.println(methodGet.composeMessage());
	}

}
