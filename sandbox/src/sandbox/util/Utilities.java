package sandbox.util;

/**
 * Miscellaneous utilities added in as needed
 */
public class Utilities 
{
	
    public static String StringEmpty = "";
    static final int MAX_ARRAY_LENGTH = 100;
    
    /**
     * Determines if the passed in string is null or
     * the empty string.
     * @param string The string to determine if null or empty
     * @return True if the passed in string is null or empty, False if not.
     */
    public static boolean StringIsNullOrEmpty(String string)
    {
        if (string == null)
        {
            return true;
        }
        if (string.equals(Utilities.StringEmpty))
        {
            return true;
        }
        
        return false;
    }
    public static String [] split(String s,String delim)
    {
        String [] temp = new String [MAX_ARRAY_LENGTH]; // all elements null
        int index = 0;
        int startAt = 0;
        int stopBefore = s.indexOf(delim, 0);
        int slen = s.length();
        
        // if last char is the delim remove it . cause a crash
        if(s.endsWith(delim))
        {
            s = s.substring(0,slen-1);
        }
        
        while (stopBefore > -1 && index < temp.length)
        {
            temp [index] = s.substring (startAt, stopBefore);
            index++;
            startAt = stopBefore + 1;
            
            while (s.substring (startAt, startAt + 1).equals(delim))
            {
                    startAt++;
                    if(startAt == slen)
                        break;
            }
            if(startAt != slen)  stopBefore = s.indexOf (delim, startAt);
        }
        if (startAt < s.length () && index < temp.length)
            temp [index] = s.substring (startAt, s.length ());
        String [] result = new String [index + 1]; // no longer than necessary
        index = 0;
        while (index < temp.length && temp [index] != null)
        {
            result [index] = temp [index];
            index++;
        }
        temp = null;
        return result;
    }

}
