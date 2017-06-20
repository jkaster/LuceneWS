package com.codegear.dn.search.engine;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

public class LuceneIndexer extends Thread {
	private String indexDir = null;
	
	/** Queue that contains the documents to be indexed. */
	private BlockingQueue<QueueJob> queue = new LinkedBlockingQueue<QueueJob>();
	
	/** Internal IndexWriter instance. */
	private IndexWriter writer = null;
	
	/** Atomic variable that indicates whether the index is being optimized. */
	private AtomicBoolean isOptimizing = new AtomicBoolean();
	
	/** Global instance of the indexer. */
	private static LuceneIndexer instance = null;
	
	/** Constructs an indexer instance for the specified index directory location. */
	protected LuceneIndexer( String directory ) {
		super();
		
		System.out.println( "Creating LuceneIndexer..." );
		
		indexDir = directory;
		
		try
		{
			// First check whether the index is locked.
			if( IndexReader.isLocked( indexDir ) )
			{
				// This should only happen if Lucene didn't clean up properly, and
				// left the indexed locked.
				Log.error( "Index '" + indexDir + "' is already locked. Forcing unlock." );
			
				// Force the unlock.
				IndexReader.unlock( FSDirectory.getDirectory( indexDir ) );
			}
			
			// Start the index writer.
			writer = new IndexWriter( indexDir, new StandardAnalyzer() );
		}
		catch( Exception e )
		{
			// Log the exception.
			Log.error( "Failed to initialize IndexWriter.", e );
		}
	}
	
	public static void createInstance( String directory ) {
		instance = new LuceneIndexer( directory );
		instance.start();
	}
	
	/** Indexes the content specified by the fields parameter, deleting any prior document
	 * with the same id. */ 
	public void indexContent( ContentFields fields ) throws EngineException {
		try
		{
			queue.add( QueueJob.createIndexJob( fields ) );
		}
		catch( Exception e )
		{
			// Log the exception.
			Log.error( "Failed to add content to queue." , e );
		}
	}
	
	/** Deletes the specified content from the index. */
	public void deleteContent( String appID, String contentID ) throws EngineException {
		try
		{
			queue.add( QueueJob.createDeleteJob( appID, contentID ) );
		}
		catch( Exception e )
		{
			// Log the exception.
			Log.error( "Failed to add content to queue." , e );
		}
	}
	
	/** Executes the indexing operations. */
	public void run() {
		try
		{
			int alteredDocumentCount = 0;
			QueueJob job = null;
			
			while( !interrupted() )
			{
				// Retrieve the the QueueJob item from the collection.
				job = queue.poll( 30, TimeUnit.SECONDS );
				if( job != null )
				{
					if( job.getJobType() == QueueJob.INDEX_JOB )
						addContentToIndex( job.getFields() );
					else if( job.getJobType() == QueueJob.DELETE_JOB )
						deleteContentFromIndex( job.getAppID(), job.getContentID() );
					
					++alteredDocumentCount;
				}
				
				if( job == null )					
				{
					// If no new item is added for 30 seconds and a substantial number of 
					// Documents have been added, optimize the index.
					if( alteredDocumentCount > 400 )
					{
						optimizeIndex();
						alteredDocumentCount = 0;
					}
					else if( alteredDocumentCount > 0 )
					{
						// Otherwise, flush the index to disk.
						flushIndex();
					}
				}
			}
		}
		catch( InterruptedException ie )
		{
			Log.info( "IndexWriter thread interrupted." );
		}
		
		Log.info( "Indexer thread shutting down." );
		
		// When the thread exits, close the writer.
		closeWriter();
	}
	
	/** Indicates whether the index is currently being optimized. If it is, readers must not
	 * refresh to save disk space. */
	public boolean getIsOptimizing() {
		return isOptimizing.get();
	}
	
	/** Returns the global indexer instance. */
	public static LuceneIndexer getInstance() {
		return instance;
	}
	
	/** Closes the writer and flushes all changes to disk. */
	public void closeWriter() {
		Log.info( "Closing writer..." );
		
		try
		{
			writer.close();
		}
		catch( Exception e )
		{
			// Log the exception.
			Log.error( "Exception thrown while closing writer.", e );
		}
		
		writer = null;
	}
	
	/** Adds the specified document to the index. */
	private void addContentToIndex( ContentFields fields ) {
		try
		{
			if( writer == null )
			{
				// This should never happen, but if it does, log it.
				Log.error( "writer instance is null. Cannot index document (" + fields.getLuceneID() + ")" );
				return;
			}
			
			// First make sure there are no previous documents with this ID.
			writer.deleteDocuments( new Term( ContentFields.ID_FIELD, fields.getLuceneID() ) );
		
			// Now add the content to the index.
			writer.addDocument( fields.asDocument() );
		}
		catch( Exception e )
		{
			// If an exception is thrown, it means that this content has not been indexed.
			// So we log the error, along with the information about this document.
			Log.error( "addContentToIndex failed for document (" + fields.getLuceneID() + ")", e );
		}
	}
	
	/** Deletes the specified content from the index. */
	private void deleteContentFromIndex( String appID, String contentID ) {
		try
		{
			if( writer == null )
			{
				// This should never happen, but if it does, log it.
				Log.error( "writer instance is null. Cannot delete document (" + ContentFields.generateLuceneID( appID, contentID ) + ")" );
				return;
			}
			
			// Delete the documents that match the specified appID and contentID.
			writer.deleteDocuments( new Term( ContentFields.ID_FIELD, ContentFields.generateLuceneID( appID, contentID ) ) );
		}
		catch( Exception e )
		{
			// If an exception is thrown, it means that this content has not been indexed.
			// So we log the error, along with the information about this document.
			Log.error( "deleteContentFromIndex failed for document (" + ContentFields.generateLuceneID( appID, contentID ) + ")", e );
		}
	}
	
	/** Optimizes the index. */
	private void optimizeIndex() {
		// First indicate that we are optimizing.
		isOptimizing.set( true );
		try
		{
			if( writer == null )
			{
				// This should never happen, but if it does, log it.
				Log.error( "writer instance is null. Cannot optimize index." );
				return;
			}
			
			Log.info( "Optimizing index." );
			writer.optimize();
			writer.flush();
		}
		catch( Exception e )
		{
			// Log the exception.
			Log.error( "Index optimize failed.", e );
		}
		finally
		{
			isOptimizing.set( false );
		}
	}
	
	/** Flushes the index to disk so that readers will see the changes. */
	private void flushIndex() {
		try
		{
			writer.flush();
		}
		catch( Exception e )
		{
			// Log the exception.
			Log.error( "Index flush failed.", e );
		}
	}
}

/** Class that encapsulates a single job to be performed by the indexer. */
class QueueJob {
	/** Index content job type. */
	public static final int INDEX_JOB = 0;
	/** Delete content job type. */
	public static final int DELETE_JOB = 1;
	
	private int jobType = -1;	
	private ContentFields fields = null;
	private String appID = null;
	private String contentID = null;
	
	/** Create an indexing job instance. */
	public static QueueJob createIndexJob( ContentFields fields ) {
		QueueJob result = new QueueJob();
		
		result.jobType = QueueJob.INDEX_JOB;
		result.fields = fields;
		
		return result;
	}
	
	/** Create a delete content job instance. */
	public static QueueJob createDeleteJob( String appID, String contentID ) {
		QueueJob result = new QueueJob();
		
		result.jobType = QueueJob.DELETE_JOB;
		result.appID = appID;
		result.contentID = contentID;
		
		return result;
	}
	
	/** Return the job type. */
	public int getJobType() {
		return jobType;
	}
	
	/** Return the fields for the document that is to be indexed. */
	public ContentFields getFields() {
		return fields;
	}
	
	/** Return the application ID of the content to be deleted. */
	public String getAppID() {
		return appID;
	}
	
	/** Return the content ID of the content to be deleted. */
	public String getContentID() {
		return contentID;
	}
	
	private QueueJob() {
	}
}