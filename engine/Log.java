package com.codegear.dn.search.engine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Simple logging implementation */
public class Log {
	private static Logger logger = Logger.getLogger( Log.class.getName() );
	private static SimpleDateFormat dateFormat = new SimpleDateFormat( "hh:mm:ss a yyyy.MM.dd" );
	
	public static void error( String str ) {
		logger.severe( getTimestamp() + " " + str );
	}
	
	public static void error( String str, Throwable t ) {
		logger.log(Level.SEVERE, getTimestamp() + " " + str, t );
	}
	
	public static void info( String str ) {
		logger.info( getTimestamp() + " " + str );
	}
	
	public static void debug( String str ) {
		logger.fine( getTimestamp() + " " + str );
	}
	
	private static String getTimestamp() {
		return dateFormat.format( new Date( System.currentTimeMillis() ) );
	}
}
