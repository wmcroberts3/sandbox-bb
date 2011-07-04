package sandbox.websockets;


public interface WebSocketEventHandler
{
	public void onOpen();
	
	
	public void onMessage(WebSocketMessage message);
	
	
	public void onClose();
}
