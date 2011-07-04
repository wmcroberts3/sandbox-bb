package sandbox.websockets;

import java.io.IOException;
import java.io.InputStream;

// import java.util.ArrayList;
// import java.util.List;
import java.util.Vector;

public class WebSocketReceiver
		extends Thread
{
	private InputStream input = null;
	private WebSocketConnection websocket = null;
	private WebSocketEventHandler eventHandler = null;

	private volatile boolean stop = false;

	
	public WebSocketReceiver(InputStream input, WebSocketConnection websocket)
	{
		this.input = input;
		this.websocket = websocket;
		this.eventHandler = websocket.getEventHandler();
	}


	public void run()
	{
		boolean frameStart = false;
		// List<Byte> messageBytes = new ArrayList<Byte>();
		Vector messageBytes = new Vector();

		while (!stop) {
			try {
				int b = input.read();
				if (b == 0x00) {
					frameStart = true;
				}
				else if (b == 0xff && frameStart == true) {
					frameStart = false;
					// Byte[] message = messageBytes.toArray(new Byte[messageBytes.size()]);
					Byte[] message = new Byte[messageBytes.size()];
					messageBytes.copyInto(message);
					
					eventHandler.onMessage(new WebSocketMessage(message));
					
					// messageBytes.clear();
					messageBytes.removeAllElements();
				}
				else if (frameStart == true){
					// messageBytes.add((byte)b);
					messageBytes.addElement(new Byte((byte)b));
				}
				else if (b == -1) {
					handleError();
				}
			}
			catch (IOException ioe) {
				handleError();
			}
		}
	}
	
	
	public void stopit()
	{
		stop = true;
	}
	
	
	public boolean isRunning()
	{
		return !stop;
	}
	
	
	private void handleError()
	{
		stopit();
		websocket.handleReceiverError();
	}
}
