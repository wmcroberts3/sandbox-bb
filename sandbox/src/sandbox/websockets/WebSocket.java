package sandbox.websockets;


public interface WebSocket
{
	public void setEventHandler(WebSocketEventHandler eventHandler);
	
	public WebSocketEventHandler getEventHandler();
	

	public void connect()
			throws WebSocketException;
	

	public void send(String data)
			throws WebSocketException;
	
//
//	public void send(byte[] data)
//			throws WebSocketException;
	

	public void close()
			throws WebSocketException;
}
