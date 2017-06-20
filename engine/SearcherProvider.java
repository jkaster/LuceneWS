package com.codegear.dn.search.engine;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

/** Thread based class that automatically recreates the IndexSearcher
 * after predefined intervals. */
public class SearcherProvider extends Thread {
	/** The index directory. */
	private String indexDir = null;
	
	/** The internal Searcher instance. */
	private IndexSearcher searcher = null;
	
	public SearcherProvider( String directory ) {
		indexDir = directory;
		
		createSearcher();
	}
	
	/** Performs the searcher recreation. */
	public void run() {
		try
		{
			while( true )
			{
				// Wait 2 minutes before checking the searcher for aging.
				Thread.sleep( 120000 );
				
				if( LuceneIndexer.getInstance() == null )
					return;
				
				// If the indexer is not optimizing, and the index is not current anymore
				// re-create the index.
				if( !LuceneIndexer.getInstance().getIsOptimizing() )
				{
					try
					{
						if( searcher.getIndexReader().isCurrent() )
							continue;
					}
					catch( Exception e )
					{
						Log.error( "Failed to determine index status.", e );
						continue;
					}

					createSearcher();
				}
				else
				{
					Log.info( "Indexer is optimizing, delaying searcher update." );
				}
			}
		}
		catch( InterruptedException ie )
		{
		}
	}
	
	/** Retrieves the searcher instance. */
	public Searcher getSearcher() {
		synchronized( this )
		{
			return searcher;
		}
	}
	
	/** Retrieves the underlying index reader instance. */
	public IndexReader getReader() {
		synchronized( this )
		{
			return searcher.getIndexReader();
		}
	}
	
	/** Creates or re-creates the searcher. */
	private void createSearcher() {
		// First create temporary instance.
		IndexSearcher tempSearcher;
		
		Log.info( "Creating searcher instance..." );
		
		try
		{
			tempSearcher = new IndexSearcher( indexDir );
		}
		catch( Exception e )
		{
			Log.error( "Exception failed while creating searcher.", e );
			return;
		}
		
		synchronized( this )
		{
			// Now close the existing searcher.
			if( searcher != null )
			{
				try
				{
					searcher.close();
				}
				catch( Exception e )
				{
					Log.error( "Exception thrown while closing searcher.", e );
				}
			}
			
			searcher = tempSearcher;
		}
	}
}
