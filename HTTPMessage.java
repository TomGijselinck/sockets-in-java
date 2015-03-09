
public abstract class HTTPMessage {
	
	public HTTPMessage() {
		this.messageBody = "";
	}
	
	/*
	 * Return all headers of this HTTP message if it exists.
	 */
	public String getHeaders() {
		return headers;
	}
	
	public void addAsHeader(String header, String value) {
		//TODO: work out
	}
	
	/*
	 * Variable referencing all header lines of this HTTP message.
	 * TODO: maak hier een linked list of bibliotheek van
	 */
	private String headers;
	
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

}
