package HTTP.message;
/**
 * An enumeration introducing different HTTP methods used to express
 * HTTP methods used in HTTP request status lines.
 * 
 * @author Tom Gijselinck
 *
 */

public enum HTTPMethod {
	HEAD, GET, PUT, POST;

	public static HTTPMethod parseMethod(String string) {
		HTTPMethod method = HEAD;
		if(string.contentEquals("HEAD")) {
			method = HEAD;
		} else if (string.contentEquals("GET")) {
			method = GET;
		} else if (string.contentEquals("PUT")) {
			method = PUT;
		} else if (string.contentEquals("POST")) {
			method = POST;
		}
		return method;
	}

}
