import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

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
	
	public HTTPRequestMessage() {
		super();
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
	
	public void setRequestLine(String request) {
		parseRequestLine(request);
	}

	private void parseRequestLine(String requestLine) {
		String[] splitString = requestLine.split(" ", 3);
		setMethod(HTTPMethod.parseMethod(splitString[0]));
		setLocalPathRequest(splitString[1]);
		setHTTPVersion(splitString[2]);
	}

	public String composeMessage() {
		String message = getRequestLine() + "\r\n";
		//loop over all headers and add them to message
		for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
			String header = entry.getKey() + ": " + entry.getValue() + "\r\n";
			message += header;
		}
		message += "\r\n";
		return message;
	}
	
	public void setIfModifiedSinceHeader(Date date) {
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		addAsHeader("If-Modified-Since", dateFormat.format(date));
		
	}
	
	public Date getIfModifiedSinceDate() throws ParseException {
		String dateString = getHeaderValue("If-Modified-Since");
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		Date date = dateFormat.parse(dateString);
		return date;
	}

	public boolean isHTTP1_0() {
		return getHTTPVersion().contains("1.0");
	}
	
	public boolean isHTTP1_1() {
		return getHTTPVersion().contains("1.1");
	}

}