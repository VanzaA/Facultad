package jade.imtp.leap;

public class ICPDispatchException extends ICPException {

	private int sessionId = -1;
	
	public ICPDispatchException(String msg, int sessionId) {
		super(msg);
		this.sessionId = sessionId;
	}

	public ICPDispatchException(String msg, Throwable nested, int sessionId) {
		super(msg, nested);
		this.sessionId = sessionId;
	}

	public int getSessionId() {
		return sessionId;
	}
}
