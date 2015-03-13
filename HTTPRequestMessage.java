import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.SimpleFormatter;

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

}