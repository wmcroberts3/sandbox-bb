package sandbox.websockets;

import java.io.UnsupportedEncodingException;


public class WebSocketMessage
{
	private Byte[] message;
	

	public WebSocketMessage(final Byte[] message)
	{
		this.message = message;
	}


	public String getText()
	{
		byte[] message = new byte[this.message.length];
		for (int i = 0; i < this.message.length; i++) {
			message[i] = this.message[i].byteValue();
		}
		try {
			return new String(message, "UTF-8");
		}
		catch (UnsupportedEncodingException uee) {
			return null;
		}
	}
}
