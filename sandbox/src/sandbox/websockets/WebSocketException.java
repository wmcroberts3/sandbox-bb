package sandbox.websockets;


public class WebSocketException
		extends Exception
{
	private static final long serialVersionUID = 1L;


	public WebSocketException(String message)
	{
		super(message);
	}
	
	
	public WebSocketException(String message, Throwable t)
	{
		// super(message, t);
		super(message);
	}
}
