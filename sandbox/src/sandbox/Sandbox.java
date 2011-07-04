package sandbox;

import net.rim.device.api.ui.UiApplication;

import net.rim.device.api.command.*;
import net.rim.device.api.io.*;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.BorderFactory;
import net.rim.device.api.util.*;

import javax.microedition.io.*;
import java.util.*;
import java.io.*;
import javax.wireless.messaging.*;

import sandbox.log.Log;
import sandbox.ui.AppTheme;
import sandbox.ui.helpers.CustomImageButtonField;
import sandbox.ui.helpers.SpacerField;

/**
 * A coding challenge to create a simple app for GroupMe with the following requirements:
 * 1. Prepopulate a text field with "I love GroupMe".
 * 2. Allow for changing of the text.
 * 3. Allow for a phone number to be specified.
 * 4. Allow for sending of the text via SMS. Sending of the SMS must be from within the app
 *    and not via an Invoke or other means.
 */
public class Sandbox extends UiApplication  
{  
    private static final int MAX_PHONE_NUMBER_LENGTH = 32; 
    private static final int MAX_MESSAGE_LENGTH = 140;
    
    private static final String NON_ZERO_PORT_NUMBER = "3590";    
        
    private EditField              _sendText;
    private EditField              _address;
    private CustomImageButtonField _sendButton;
    private EditField              _status;
    private ListeningThread        _listener;
    private SendingThread             _sender;            
    private Connection             _conn; 
    private String                 _port = "0";
    private String                 _defaultText = "I love GroupMe!";
    private MenuItem               _sendMenuItem;
    private HorizontalFieldManager _headerMgr;
    private BitmapField            _headerLogo;
    private VerticalFieldManager   _phoneInputMgr;
    private VerticalFieldManager   _messageInputMgr;
    private VerticalFieldManager   _statusOutputMgr;
	private ListField              _messageItemsList;
	private int                    _lineHeight;
	private Vector                 _messages;
    

    /**
     * Entry point for application
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args)
    {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        Sandbox smsTest = new Sandbox();
        smsTest.enterEventDispatcher();
    }

    /**
     * Creates a new SMSTest object
     */
    public Sandbox()
    {
    	// Get our listener thread (not important for this challenge) and our
    	// sending threads going
        invokeLater(new Runnable()
        {
            
            public void run()
            {
                if(!isCDMA())
                {
                    int result = Dialog.ask(Dialog.D_YES_NO, "Send messages to inbox?", Dialog.YES);
                    if (!(result == Dialog.YES))
                    {
                        // If user chooses to not have message routed to inbox,
                        // we need to specify an arbitrary non-zero port number.
                        _port = NON_ZERO_PORT_NUMBER;
                    }          
                }
                
                _listener = new ListeningThread();
                _listener.start();                  
        
                _sender = new SendingThread();
                _sender.start();     
            }
        });          
        
        // Ok, we are good to go to display our main screen
        SandboxScreen screen = new SandboxScreen();
        pushScreen(screen);
    }
    
    /**
     * A thread to manage sending SMS messages
     */
    private class SendingThread extends Thread
    {
        private boolean _stopped = false;        

        // Create a vector of SmsMessage objects with an initial capacity of 5.
        private Vector _msgs = new Vector(5);       

        
        /**
         * Queues message send requests to send later
         * @param address The address to send the message to
         * @param msg The message to send
         */
        public void send(String address, String msg, String port)
        {
            SmsMessage message = new SmsMessage(address, msg, port);
            synchronized (this._msgs)
            {
                if (! this._stopped)
                {
                    this._msgs.addElement(message);
                    this._msgs.notifyAll();
                }
            }
        }


        /**
         * Stops this thread from sending any more messages
         */
        public void stopSendingThread()
        {
            synchronized (this._msgs)
            {
                this._stopped = true;
                this._msgs.notifyAll();
                this._msgs.removeAllElements();       

                try
                {
                    if ( _conn != null )
                    {
                        _conn.close();
                    }
                } 
                catch (IOException ioe ) 
                {
                	Log.event("ST:SST:" + ioe.toString(), Log.ERROR);
                }             
            }
        }
        

        /**
         * Sends any queued messages until stopSendingThread() is called
         */
        public void run()
        {
            while (true)
            {
                final SmsMessage smsMessage;
                synchronized (this._msgs)
                {
                    if (this._stopped)
                    {
                        return;
                    }
                    else if (this._msgs.isEmpty())
                    {
                        try
                        {
                            this._msgs.wait();
                        }
                        catch (InterruptedException ie)
                        {
                            return;
                        }
                    }                    

                    if (this._stopped)
                    {
                        return;
                    }
                    else
                    {
                        smsMessage = (SmsMessage) this._msgs.elementAt(0);
                        this._msgs.removeElementAt(0);
                    }
                }
                try
                {
                    if(isCDMA())
                    {
                        DatagramConnectionBase dcb = (DatagramConnectionBase)_conn; 
                        dcb.send(smsMessage.toDatagram(dcb));
                    }
                    else
                    {
                        MessageConnection mc = (MessageConnection)_conn;
                        mc.send(smsMessage.toMessage(mc));
                    }
                    
                    // First let's do what's necessary for our list of messages display
                    updateStatus("");
                    
                    // Now make it convenient for the user to send another message
                    updateScreen();

                }
                catch (IOException ioe) 
                {                    
                    updateStatus(ioe.toString());
                }
            }
        }
    }
    
    /**
     * This screen acts as the main screen to allow the user to send SMS messages.
     * 
     * Here is where you would start to expand to hook in the listener if you wanted
     * to take this app further.
     */
    private class SandboxScreen extends MainScreen
    {      
        /**
         * Default constructor
         */
        private SandboxScreen()
        {
        	// Construct our header (GroupMe logo)
            _headerMgr = new HorizontalFieldManager(HorizontalFieldManager.USE_ALL_WIDTH);
            Background headerBackground = 
            	BackgroundFactory.createLinearGradientBackground(AppTheme.headerTopGradient,     // colorTopLeft 
            		                                             AppTheme.headerTopGradient,     // colorTopRight, 
            		                                             AppTheme.headerBottomGradient,  // colorBottomRight, 
            		                                             AppTheme.headerBottomGradient); // colorBottomLeft);
            
            _headerMgr.setBackground(headerBackground);
            _headerLogo = new BitmapField();
    		Bitmap img = Bitmap.getBitmapResource("groupme_header.png");
    		if(img != null) 
    		{
    			_headerLogo.setBitmap(img);
    		}
    		_headerMgr.add(_headerLogo);

    		// Construct our phone input banner (specify phone number to send to)
    		_phoneInputMgr = new VerticalFieldManager(HorizontalFieldManager.USE_ALL_WIDTH);
            Background phoneInputBackground = 
            	BackgroundFactory.createLinearGradientBackground(AppTheme.phoneTopGradient,     // colorTopLeft 
            		                                             AppTheme.phoneTopGradient,     // colorTopRight, 
            		                                             AppTheme.phoneBottomGradient,  // colorBottomRight, 
            		                                             AppTheme.phoneBottomGradient); // colorBottomLeft);
            
            _phoneInputMgr.setBackground(phoneInputBackground);
            _address = new EditField("Phone: ", "", MAX_PHONE_NUMBER_LENGTH, EditField.FILTER_PHONE);
            Background phoneFieldBackground = BackgroundFactory.createSolidBackground(AppTheme.editFieldFill);
            _address.setBackground(phoneFieldBackground);
            Bitmap borderBitmap = Bitmap.getBitmapResource("rounded_border.png");
            _address.setBorder(BorderFactory.createBitmapBorder(new XYEdges(12,12,12,12), borderBitmap));
    		_phoneInputMgr.add(new SpacerField(8));
    		_phoneInputMgr.add(_address);
    		_phoneInputMgr.add(new SpacerField(8));

    		// Construct our message banner (create SMS message here)
    		_messageInputMgr = new VerticalFieldManager(HorizontalFieldManager.USE_ALL_WIDTH);
    		_sendText = new EditField();
            _sendText.setText(_defaultText);
            _sendText.setBorder(BorderFactory.createBitmapBorder(new XYEdges(12,12,12,12), borderBitmap));
            _sendButton = new CustomImageButtonField("refresh_icon_on.png",
                    "refresh_icon_off.png",
                    AppTheme.bodyFill);

            _sendButton.setChangeListener
            (
                new FieldChangeListener() 
            	{
                    public void fieldChanged(Field field, int context)
                    {
            	        commandSend();
                    }
            	}
            );
    		_messageInputMgr.add(new SpacerField(8));
    		_messageInputMgr.add(new LabelField("Message:"));
    		_messageInputMgr.add(_sendText);
    		_messageInputMgr.add(new SpacerField(4));
    		_messageInputMgr.add(_sendButton);
    		_messageInputMgr.add(new SpacerField(4));

    		// Construct our status output banner (dynamically updating list field)
    		_statusOutputMgr = new VerticalFieldManager(HorizontalFieldManager.USE_ALL_WIDTH);
            Background statusOutputBackground = 
            	BackgroundFactory.createLinearGradientBackground(AppTheme.statusTopGradient,     // colorTopLeft 
            		                                             AppTheme.statusTopGradient,     // colorTopRight, 
            		                                             AppTheme.statusBottomGradient,  // colorBottomRight, 
            		                                             AppTheme.statusBottomGradient); // colorBottomLeft);
            
            _statusOutputMgr.setBackground(statusOutputBackground);
    		_messages         = new Vector();
    		_messageItemsList = new ListField();
    		Background messageListFieldBackground = BackgroundFactory.createSolidBackground(AppTheme.editFieldFill);
            _messageItemsList.setBackground(messageListFieldBackground);
            _messageItemsList.setBorder(BorderFactory.createBitmapBorder(new XYEdges(12,12,12,12), borderBitmap));
            _lineHeight       = _messageItemsList.getRowHeight();
            _messageItemsList.setRowHeight(_lineHeight * 2);
            _messageItemsList.setEmptyString("", 0);
            _messageItemsList.setCallback(new ListFieldCallback() 
    	        {
    	            public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) 
    	            {
    	            	messageItemsDrawListRow(listField, graphics, index, y, width);
    	            }
    	            public int getPreferredWidth(ListField listField)
    	            {
    	                return messageItemsGetPreferredWidth(listField);
    	            }
    	            public Object get(ListField listField, int index)
    	            {
    	                return messageItemsGet(listField, index);
    	            }
    	            public int indexOfList(ListField listField, String prefix, int start) 
    	            {
    	                return messageItemsIndexOfList(listField, prefix, start);
    	            }
    	        }
            );
    		_statusOutputMgr.add(_messageItemsList);
            
            // Now build the GUI
            add(_headerMgr);
            add(_phoneInputMgr);
            add(_messageInputMgr);
            add(_statusOutputMgr);
            
            // Complement the GUI with a menu to Send as well
            _sendMenuItem = new MenuItem(new StringProvider("Send"), 0x230010, 0);
            _sendMenuItem.setCommand(new Command(new CommandHandler() 
            	{
	                /**
	                 * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata, Object)
	                 */
	                public void execute(ReadOnlyCommandMetadata metadata, Object context) 
	                {   
	                	commandSend();
	                }
            	}
            )
            );
        }
        
        /**
         * Draw our dynamically updating list field elements.
         * 
         * Phone number in bold and text (abbreviated) in normal.
         */
        public void messageItemsDrawListRow(ListField listField,
                Graphics graphics,
                int index,
                int y,
                int width)
        {

        	// sanity check
        	if(index >= _messages.size()) 
        	{
        		return;
        	}

        	// Get our value object for the message
        	MessageEnvelope envelope = (MessageEnvelope)_messages.elementAt(index);

        	// Display the phone number in bold
        	Font origFont = graphics.getFont();
        	graphics.setFont(origFont.derive(Font.BOLD));
        	String firstLine = "";
        	graphics.drawText(envelope._messageAddress, 20, y,
    				(int)(getStyle() | DrawStyle.ELLIPSIS),
    				-1);

        	// Display the text (abbreviated possibly) in normal
        	graphics.setFont(origFont.derive(Font.PLAIN));
        	graphics.drawText(envelope._messageText, 20, y+_lineHeight,
        			(int)(getStyle() | DrawStyle.ELLIPSIS),
        			-1);
        	graphics.setFont(origFont);
        }

        public int messageItemsGetPreferredWidth(ListField listField) 
        {
        	return Display.getWidth();
        }

        /**
         * Return our value object from our backing collection.
         */
        public Object messageItemsGet(ListField listField, int index) 
        {
        	return (Object)_messages.elementAt(index);
        }

        /**
         * Not implemented
         */
        public int messageItemsIndexOfList(ListField listField,
               String prefix,
               int start)
        {
        	return 0;
        }

        /**
         * Centralized command handler for sending SMS messages
         */
        private void commandSend()
        {
	    	try
	    	{
	    		// What and where do we want to send
	            String text = _sendText.getText();
	            String addr = _address.getText();                  
	            
	            // Validity checks
	            if (addr.length() == 0)
	            {
	                Dialog.alert("Destination field cannot be blank");
	                _address.setFocus();
	            }
	            else if(text.length() == 0)
	            {
	                Dialog.alert("Message field cannot be blank");
	                _sendText.setFocus();                
	            } 
	            else
	            {
	            	// Ok, good to go, add the message to the queue
	                _sender.send(addr, text, _port);                
	            }
	    	}
	    	catch(Exception ex)
	    	{
	    		Log.event("ST:LS:" + ex.toString(), Log.ERROR);
	    	}
        }
        
        /**
         * Only build main menu (no context menu off of trackball)
         */
        protected void makeMenu(Menu menu, int instance) 
        {
        	// Only build main menu (no context menu off of trackball)
        	if (instance == 0)
        	{
    	        menu.add(_sendMenuItem);
        	}
        	
   	        super.makeMenu(menu, instance);
        }

        /**
         * Prevent the save dialog from being displayed
         */
        public boolean onSavePrompt()
        {
            return true;
        }    

        
        /**
         * Closes the application
         */
        public void close()
        {
        	// Close out threads, try/catch handled there
            _listener.stopListeningThread();                               
            _sender.stopSendingThread();           
            
            super.close();
        }
    }
    

    

    /**
     * Update the GUI with the data just received.
     * 
     * We will be dynamically updating the list field of messages.
     * 
     * @param msg Optional status message to display on screen (not implemented)
     */
    private void updateStatus(final String msg)
    {
        invokeLater(new Runnable() 
        {
            /**
             * Update our backing store of messages and the list field
             * that displays them.
             */
            public void run()
            {
            	// Create a value object for the message
                MessageEnvelope envelope = new MessageEnvelope();
                envelope._messageAddress = _address.getText();
                envelope._messageText    = _sendText.getText();
                
                // Update the backing store
                _messages.insertElementAt(envelope, 0);
                
                // Update the list field that displays the backing store
                _messageItemsList.insert(0);
            }
        });

    }
    
    /**
     * Update the GUI so the user is ready to send another message
     */
    private void updateScreen()
    {
        invokeLater(new Runnable() 
        {
            /**
             * Updates the GUI to be ready to send another message
             */
            public void run()
            {
                // Clear the field where we enter text 
            	// TODO: Use clear() instead?
            	// and make sure we are focused to send another message
            	_sendText.setText("");
            	_sendText.setFocus();
            }
        });

    }

    /**
     * Some simple formatting for a received SMS message
     * @param m The message just received
     */
    private void receivedSmsMessage(Message m)
    {
        String address = m.getAddress();
        String msg = null;
        
        if ( m instanceof TextMessage )
        {
            TextMessage tm = (TextMessage) m;
            msg = tm.getPayloadText();
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("Received:");
        sb.append('\n');
        sb.append("Destination:");
        sb.append(address);
        sb.append('\n');
        sb.append("Data:");
        sb.append(msg);
        sb.append('\n');

        updateStatus(sb.toString());
    }
    
    /**
     * Determines whether the currently active WAF is CDMA
     * @return True if currently active WAF is CDMA, otherwise false
     */
    private static boolean isCDMA()
    {
        return (RadioInfo.getActiveWAFs() & RadioInfo.WAF_CDMA) == RadioInfo.WAF_CDMA;
    }

    /**
     * Our value class for an SMS message, used by the SendThread class
     */
    private static final class SmsMessage
    {
        private String _address;  
        private String _port;      
        private String _msg;    
        
        
        /**
         * Creates a SMS message
         * @param address The address of the recipient of the SMS message
         * @param msg The message to send
         */
        public SmsMessage(String address, String msg, String port)
        {
            _address = address;   
            _port = port;         
            _msg = msg;
        }
        
        
        /**
         * Returns a Message object representing this SMS message
         * @param mc The MessageConnection source with which to create the Message from
         * @return The Message object representing the SMS message
         */
        public Message toMessage(MessageConnection mc)
        {      
            // If the user chose to have messages routed to the inbox (port = 0),
            // we need to specify an address without a port number.
            String addressString = "//" + _address + ( _port.equals(NON_ZERO_PORT_NUMBER) ?  ":" + _port : "" );
            
            TextMessage m = (TextMessage) mc.newMessage(MessageConnection.TEXT_MESSAGE , addressString);
            m.setPayloadText(_msg);
            
            return m;
        }
        
        
        /**
         * Returns a Datagram object representing this SMS message
         * @param datagramConnectionBase The DatagramConnectionBase object with which to create the Datagram from
         * @return The Datagram object representing the SMS message
         */
        public Datagram toDatagram(DatagramConnectionBase datagramConnectionBase) throws IOException
        {       
            DatagramBase datagram = null;                            
            byte[] data = _msg.getBytes("ISO-8859-1");
            datagram = (DatagramBase) datagramConnectionBase.newDatagram();
            SmsAddress smsAddress = new SmsAddress("//" + _address); 
            SMSPacketHeader smsPacketHeader = smsAddress.getHeader();
            smsPacketHeader.setMessageCoding(SMSPacketHeader.MESSAGE_CODING_ISO8859_1);
            datagram.setAddressBase(smsAddress);            
            datagram.write(data, 0, data.length);         
            
            return datagram;
        }
    }

    /**
     * Value object used to encapsulate state of a message sent
     */
    private class MessageEnvelope
    {
    	private String _messageAddress;
    	private String _messageText;
    }
    
    /**
     * This thread listens for any incoming messages
     */
    private class ListeningThread extends Thread
    {
        private boolean _stop;
        
        /**
         * Stops this thread from listening for messages
         */
        private synchronized void stopListeningThread()
        {
            _stop = true;
            
            try 
            {                
                if( _conn != null )
                {                    
                    _conn.close(); 
                }
            } 
            catch (IOException ioe) 
            {
            	Log.event("ST:SLT:" + ioe.toString(), Log.ERROR);
            }
        }   

        /**
         * Listens for incoming messages until stopListeningThread() is called
         */
        public void run()
        {
            try 
            {               
                _conn =  Connector.open("sms://:" + _port); 
                for(;;)
                {
                    if ( _stop )
                    {
                        return;
                    }
                    MessageConnection msgConn = (MessageConnection)_conn;
                    Message m = msgConn.receive();

                    receivedSmsMessage(m);                  
                }
            } 
            catch (IOException ioe)
            {                             
                updateStatus(ioe.toString());
            }
        }
    }
}
