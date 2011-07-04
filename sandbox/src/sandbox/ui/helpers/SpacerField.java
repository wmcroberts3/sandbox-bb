package sandbox.ui.helpers;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

/**
 * A simple field to help out in spacing your layout.
 * 
 * Use this field to add in desired space between fields.
 */
public class SpacerField extends Field 
{
    protected int _height;

    /**
     * Constructors
     */
    public SpacerField() 
    {
        this(2);
    }

    public SpacerField(int height) 
    {
        super(Field.READONLY);
        _height = height;
    }

    /**
     * Simply specify the height we want to space ourselves out to
     */
    public void layout(int width, int hieght) 
    {
        setExtent(Display.getWidth(), _height);
    }

    /**
     * No-op for paint
     */
    public void paint(Graphics g) 
    {
    }
}