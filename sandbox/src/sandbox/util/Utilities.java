package sandbox.util;

/**
 * Miscellaneous utilities added in as needed
 */
public class Utilities 
{
    public static String StringEmpty = "";

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
}
