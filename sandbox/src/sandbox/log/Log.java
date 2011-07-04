package sandbox.log;

import net.rim.device.api.system.EventLogger;

/**
 * A simplification of RIM's EventLogger class so that logging can be added 
 * in as simply as possible.
 * 
 * We generally follow these logging conventions:
 * ALWAYS_LOG: For initial start/stop procedures until the user-defined logging
 * level is set.
 * 
 * SEVERE_ERROR: Application can't proceed further and the application will exit
 * due to this error.
 * 
 * ERROR: Application can't proceed further, however the application will not
 * exit due to this error.
 * 
 * WARNING: An anomalous situation, however the application can proceed.
 * 
 * INFO: Placed at that entry and end points of public API
 * 
 * The default logging level is WARNING.
 * 
 * We utilize a cryptic prefix to our logging messages that uses the shortest
 * abbreviations possible for the class, method and possible sequence in method 
 * location. For instance, the prefix for the exception log for 
 * ArchiveScreen.ContentReaderThread.Run for the second IOException would be 
 * 
 * AS:CRT:R1:
 * 
 * This allows you to pinpoint in the code where the log came from without
 * taking up to many characters in RIM's limited logging facility.
 */
public class Log 
{
	public final static long GUID = 0x6bc611e33074e780L;
	
	/**
	 * For initial start/stop procedures until the user-defined logging
     * level is set.
	 */
	public static int ALWAYS_LOG = EventLogger.ALWAYS_LOG;
	
	/**
	 * Application can't proceed further on its current task
	 * AND the application WILL exit due to this error.
     */
	public static int SEVERE_ERROR = EventLogger.SEVERE_ERROR;
	
	/**
     * Application can't proceed further on its current task 
     * AND the application WILL NOT exit due to this error.
	 */
	public static int ERROR = EventLogger.ERROR;
	
	/**
     * An anomalous situation, however the application can proceed on
     * its current task.
	 */
	public static int WARNING = EventLogger.WARNING;
	
    /**
     * Placed at that entry and end points of public API.
	 */
	public static int INFORMATION  = EventLogger.INFORMATION;
	
	/**
	 * Generally not used.
	 */
	public static int DEBUG_INFO   = EventLogger.DEBUG_INFO;
	
	/**
	 * A trimmed down EventLogger wrapper to allow for easier entry
	 * in the code.
	 * 
	 * @param message The log message
	 * 
	 * @param level The level you want to log at. Note there are also
	 * shorter representations in this class for the EventLogger level
	 * constants.
	 */
	public static void event(String message, int level)
	{
		try
		{
			EventLogger.logEvent(GUID, message.getBytes(), level);
		}
		catch(Exception ex)
		{
			// We'll purposefully leave this exception
			// block empty so that unexpected exceptions
			// from here do not percolate up
		}
	}
}
