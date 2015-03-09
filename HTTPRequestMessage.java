/**
 * A class of HTTP request messages as special kind of HTTP messages involving
 * as additional properties a HTTP method, a path to the requested resource and
 * a HTTP version.
 * 
 * @author Tom Gijselinck
 * 
 */
public class HTTPRequestMessage extends HTTPMessage {

	public HTTPRequestMessage(HTTPMethod method, String pathRequestedResource,
			String HTTPVersion) {
		super();
		setMethod(method);
		setLocalPathRequest(pathRequestedResource);
		setHTTPVersion(HTTPVersion);
	}

	public HTTPMethod getMethod() {
		return method;
	}

	public void setMethod(HTTPMethod method) {
		this.method = method;
	}

	private HTTPMethod method;

	public String getLocalPathRequest() {
		return localPathRequest;
	}

	public void setLocalPathRequest(String path) {
		localPathRequest = path;
	}

	private String localPathRequest;

	public String getHTTPVersion() {
		return HTTPVersion;
	}

	public void setHTTPVersion(String version) {
		HTTPVersion = version;
	}

	private String HTTPVersion;

	public HTTPClient getClient() {
		return client;
	}

	public void setClient(HTTPClient client) {
		this.client = client;
	}

	private HTTPClient client;

	public String getRequestLine() {
		return method + " " + localPathRequest + " " + HTTPVersion;
	}

	//TODO pas headers aan
	public String composeMessage() {
		String message = getRequestLine() + "\r\n" + "Host: "
				+ getClient().getHost() + "\r\n\r\n";
		return message;
	}

}