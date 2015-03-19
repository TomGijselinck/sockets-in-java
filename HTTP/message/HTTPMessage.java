package HTTP.message;
import java.util.HashMap;
import java.util.Map;


public abstract class HTTPMessage {
	
	public HTTPMessage() {
		this.messageBody = "";
	}
	
	/*
	 * Return all headers of this HTTP message if it exists.
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public boolean hasAsHeader(String header) {
		return headers.containsKey(header);
	}

	public boolean hasAsHeaderValue(String header, String value) {
		if (hasAsHeader(header)) {
			return headers.get(header).trim().contentEquals(value);
		} else {
			return false;
		}
	}
	
	public String getHeaderValue(String header) {
		if(headers.containsKey(header)) {
			return headers.get(header).trim();
		} else {
			System.out.println("Warning: no header of type " + header);
			return null;
		}
	}
	
	public void addAsHeader(String header, String value) {
		headers.put(header, value);
	}
	
	public void removeAsHeader(String header) {
		headers.remove(header);
	}
	
	/*
	 * Variable referencing all header lines of this HTTP message.
	 * TODO: maak hier een linked list of bibliotheek van
	 */
	private Map<String, String> headers = new HashMap<String, String>();
	
	public String getMessageBody() {
		return messageBody;
	}
	
	public void addToMessageBody(String bodyContent) {
		try {
			messageBody += bodyContent;
		} catch (NullPointerException e) {
			//do nothing
		}
	}
	
	public void setMessageBody(String body) {
		messageBody = body;
	}
	
	/*
	 * Variable referencing the message body of this HTTP message.
	 */
	private String messageBody;
	
	public boolean containsBinaryFile() {
		return !containsTextFile();
	}
	
	public boolean containsTextFile() {
		return getHeaderValue("Content-Type").contains("text");
	}

}
