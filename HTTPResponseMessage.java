
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
		String[] splitString = statusLine.split(" ");
		setHTTPVersion(splitString[0]);
		setResponseStatusCode(Integer.parseInt(splitString[1]));
		setReasonPhrase(splitString[2]);
	}
	
	public void setStatusLine(String statusLine) {
		parseStatusLine(statusLine);
	}

}
