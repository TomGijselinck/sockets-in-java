import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FilenameUtils;


public class HTTPResponseMessage extends HTTPMessage {

	public HTTPResponseMessage() {
		super();
	}
	
	public String getHTTPVersion() {
		return HTTPVersion;
	}
	
	public void setHTTPVersion(String version) {
		HTTPVersion = version;
	}
	
	private String HTTPVersion;
	
	public int getResponseStatusCode() {
		return responseStatusCode;
	}
	
	public void setResponseStatusCode(int statusCode) {
		responseStatusCode = statusCode;
	}
	
	private int responseStatusCode;
	
	public String getReasonPhrase() {
		return reasonPhrase;
	}
	
	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}
	
	private String reasonPhrase;
	
	public String getPathRequestedResource() {
		if (pathRequestedResource.contentEquals("/")) {
			return "/index.html";
		} else {
			return pathRequestedResource;
		}
	}
	
	public void setPathRequestedResource(String path) {
		pathRequestedResource = path;
	}
	
	private String pathRequestedResource;
	
	public String getStatusLine() {
		return HTTPVersion + " " + responseStatusCode + " " + reasonPhrase;
	}
	
	private void parseStatusLine(String statusLine) {
		String[] splitString = statusLine.split(" ", 3);
		setHTTPVersion(splitString[0]);
		setResponseStatusCode(Integer.parseInt(splitString[1]));
		setReasonPhrase(splitString[2]);
	}
	
	public void setStatusLine(String statusLine) {
		parseStatusLine(statusLine);
	}
	
	public void setDate(Date date) {
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		addAsHeader("Date", dateFormat.format(date));
	}

	public String composeMessage() {
		String message = getStatusLine() + "\r\n";
		//loop over all headers and add them to message
		for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
			String header = entry.getKey() + ": " + entry.getValue() + "\r\n";
			message += header;
		}
		message += "\r\n";
		return message;
	}

	public void setContentType(String localPathRequest) {
		String fileType = FilenameUtils.getExtension(localPathRequest);
		if (fileType.contentEquals("html") || fileType.contentEquals("htm")) {
			addAsHeader("Content-Type", "text/html");
		} else {
			addAsHeader("Content-Type", fileType);
		}
	}
	
	public void setLastModifiedHeader(Date date) {
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		addAsHeader("Last-Modified", dateFormat.format(date));
	}

}
