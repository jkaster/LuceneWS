package com.codegear.dn.search.engine;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class LuceneIndexerFinalizer implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {
	}
	
	/** Called when the web application is being shutdown. */
	public void contextDestroyed(ServletContextEvent event) {
		Log.info( "Application context being shutdown..." );
		
		// Close the Lucene index writer to save all changes to disk.
		if( LuceneIndexer.getInstance() != null )
			LuceneIndexer.getInstance().closeWriter();
	}
}
