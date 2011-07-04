package sandbox.ui.helpers;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.XYEdges;
 
 
public class CustomImageButtonField extends Field implements DrawStyle 
{
    protected Bitmap _currentPicture;
    protected Bitmap _onPicture; 
    protected Bitmap _offPicture; 
    protected int    _backgroundColor;
    protected int    _height;
    protected int    _width;
 
    /**
     * Specify our image button with on/off images and background color.
     * @param onImage The image to display when highlighted
     * @param offImage The image to display when not-highlighted
     * @param backgroundColor The background color to use for this button
     */
    public CustomImageButtonField(String onImage, String offImage, int backgroundColor) 
    {
        super(Field.FIELD_HCENTER);
        
        // Grab our incoming state elements and setup
        // our calculated values (height, width, etc.)
        _backgroundColor = backgroundColor;
        _offPicture      = Bitmap.getBitmapResource(offImage);
        _onPicture       = Bitmap.getBitmapResource(onImage);
        _currentPicture  = _offPicture;
        _height          = _offPicture.getHeight();
        _width           = _offPicture.getWidth();
    }
 
    /**
     * Specify our image button with on/off images and background color along with padding
     * @param onImage The image to display when highlighted
     * @param offImage The image to display when not-highlighted
     * @param backgroundColor The background color to use for this button
     * @param padding The padding around the image
     */
    public CustomImageButtonField(String onImage, String offImage, int backgroundColor, XYEdges padding) 
    {
        this(onImage, offImage, backgroundColor );
        setPadding(padding);
    }
 
    public int getPreferredHeight() 
    {
        return _height;
    }
 
    public int getPreferredWidth() 
    {
        return _width;
    }
 
    public boolean isFocusable() 
    {
        return true;
    }
 
    protected void onFocus(int direction) 
    {
        _currentPicture = _onPicture;
        invalidate();
    }
 
    protected void onUnfocus() 
    {
        _currentPicture = _offPicture;
        invalidate();
    }
 
    protected void layout(int width, int height) 
    {
        setExtent(Math.min(width, getPreferredWidth()), Math.min(height,getPreferredHeight()));
    }
 
    /**
     * Make sure to behave like a normal button for listeners
     */
    protected void fieldChangeNotify(int context) 
    {
        try
        {
            this.getChangeListener().fieldChanged(this, context);
        }
        catch (Exception exception)
        {
        }
    }
 
    /**
     * Make sure we handle navigation events correctly
     */
    protected boolean navigationClick(int status, int time) 
    {
        fieldChangeNotify(1);
        return true;
    }
    
    /**
     * Make sure we handle keypad enters correctly
     */
    public boolean keyChar(char key, int status, int time)
	{
		boolean usedByCurrentlyFocusedField = super.keyChar(key, status, time);
		
		if (!usedByCurrentlyFocusedField)
		{
			switch(key) 
			{
				case Keypad.KEY_ENTER:
				{
					fieldChangeNotify(1);
					usedByCurrentlyFocusedField = true;
					break;
				}
			}
		}
		
		return usedByCurrentlyFocusedField;
	}

    /**
     * Simple paint of the appropriate image
     */
    protected void paint(Graphics graphics) 
    {
        graphics.setColor(_backgroundColor);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.drawBitmap(0, 0, getWidth(), getHeight(), _currentPicture, 0, 0);
    }
}
