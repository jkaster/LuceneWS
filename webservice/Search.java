package com.codegear.dn.search.webservice;

import com.codegear.dn.search.engine.ContentFields;
import com.codegear.dn.search.engine.EngineException;
import com.codegear.dn.search.engine.LuceneEngine;
import com.codegear.dn.search.engine.SearchResult;

/** Search is the public class that contains the web service methods.
 * 
 * It passes the calls onto an instance of the LuceneEngine class to
 * perform the actual index or search operations. */
public class Search {
	public Search() {
	}
	
	/**
	 * Indexes the specified content into the search.
	 * @param appID
	 * @param contentID
	 * @param author
	 * @param title
	 * @param summary
	 * @param body
	 * @param publicationDate
	 * @param languageCode
	 * @return BooleanResult
	 */
	public com.codegear.dn.search.webservice.BooleanResult indexContent( java.lang.String appID, java.lang.String contentID, java.lang.String author, java.lang.String title,
			java.lang.String summary, java.lang.String body, java.lang.String publicationDate, java.lang.String languageCode ) {
		try
		{
			LuceneEngine.getInstance().indexContent( new ContentFields( appID, contentID, author, title, summary, body, publicationDate, languageCode ) );
			
			return new BooleanResult( true, "" );
		}
		catch( EngineException ee )
		{
			return new BooleanResult( false, ee.toString() );
		}
	}
	
	public com.codegear.dn.search.webservice.BooleanResult indexHTMLContent( java.lang.String appID, java.lang.String contentID, java.lang.String author, java.lang.String title,
			java.lang.String summary, java.lang.String body, java.lang.String publicationDate, java.lang.String languageCode ) {
		try
		{
			LuceneEngine.getInstance().indexHTMLContent( new ContentFields( appID, contentID, author, title, summary, body, publicationDate, languageCode ) );
			
			return new BooleanResult( true, "" );
		}
		catch( EngineException ee )
		{
			return new BooleanResult( false, ee.toString() );
		}
	}
	
	public com.codegear.dn.search.webservice.BooleanResult indexContentEx( java.lang.String appID, java.lang.String contentID, java.lang.String author,
			java.lang.String title,	java.lang.String summary, java.lang.String body, java.lang.String publicationDate, java.lang.String languageCode,
			java.lang.String comments, java.lang.String product, java.lang.String version, java.lang.String tags, java.lang.String category,
			java.lang.String extraData, java.lang.String contentType, java.lang.String workaround ) {
		try
		{
			LuceneEngine.getInstance().indexContent(
					new ContentFields( appID, contentID, author, title, summary, body, publicationDate, languageCode,
							comments, product, version, tags, category, extraData, contentType, workaround, false ) );
			
			return new BooleanResult( true, "" );
		}
		catch( EngineException ee )
		{
			return new BooleanResult( false, ee.toString() );
		}
	}
	
	public com.codegear.dn.search.webservice.BooleanResult indexHTMLContentEx( java.lang.String appID, java.lang.String contentID, java.lang.String author,
			java.lang.String title,	java.lang.String summary, java.lang.String body, java.lang.String publicationDate, java.lang.String languageCode,
			java.lang.String comments, java.lang.String product, java.lang.String version, java.lang.String tags, java.lang.String category,
			java.lang.String extraData, java.lang.String contentType, java.lang.String workaround ) {
		try
		{
			LuceneEngine.getInstance().indexContent(
					new ContentFields( appID, contentID, author, title, summary, body, publicationDate, languageCode,
							comments, product, version, tags, category, extraData, contentType, workaround, true ) );
			
			return new BooleanResult( true, "" );
		}
		catch( EngineException ee )
		{
			return new BooleanResult( false, ee.toString() );
		}
	}
	
	public com.codegear.dn.search.webservice.BooleanResult indexContentWithSource( java.lang.String appID, java.lang.String contentID, java.lang.String author,
			java.lang.String title,	java.lang.String summary, java.lang.String body, java.lang.String publicationDate, java.lang.String languageCode,
			java.lang.String comments, java.lang.String product, java.lang.String version, java.lang.String tags, java.lang.String category,
			java.lang.String extraData, java.lang.String contentType, java.lang.String allSource, com.codegear.dn.search.engine.SourceCodeSnippet[] snippets ) {
		try
		{
			LuceneEngine.getInstance().indexContent(
					new ContentFields( appID, contentID, author, title, summary, body, publicationDate, languageCode,
							comments, product, version, tags, category, extraData, contentType, allSource, snippets, false ) );
			
			return new BooleanResult( true, "" );
		}
		catch( EngineException ee )
		{
			return new BooleanResult( false, ee.toString() );
		}
	}
	
	public com.codegear.dn.search.webservice.BooleanResult indexHTMLContentWithSource( java.lang.String appID, java.lang.String contentID, java.lang.String author,
			java.lang.String title,	java.lang.String summary, java.lang.String body, java.lang.String publicationDate, java.lang.String languageCode,
			java.lang.String comments, java.lang.String product, java.lang.String version, java.lang.String tags, java.lang.String category,
			java.lang.String extraData, java.lang.String contentType, java.lang.String allSource, com.codegear.dn.search.engine.SourceCodeSnippet[] snippets ) {
		try
		{
			LuceneEngine.getInstance().indexContent(
					new ContentFields( appID, contentID, author, title, summary, body, publicationDate, languageCode,
							comments, product, version, tags, category, extraData, contentType, allSource, snippets, true ) );
			
			return new BooleanResult( true, "" );
		}
		catch( EngineException ee )
		{
			return new BooleanResult( false, ee.toString() );
		}
	}
	
	/** Deletes a document from the index. */
	public com.codegear.dn.search.webservice.BooleanResult deleteContent( java.lang.String appID, java.lang.String contentID ) {
		try
		{
			LuceneEngine.getInstance().deleteContent( appID, contentID );
			
			return new BooleanResult( true, "" );
		}
		catch( EngineException ee )
		{
			return new BooleanResult( false, ee.toString() );
		}
	}
	
	/** Performs a search on the index. */
	public com.codegear.dn.search.engine.SearchResult search( java.lang.String queryString, int startIndex, int resultCount ) {
		try
		{
			long startTime = System.currentTimeMillis();
			
			SearchResult result = LuceneEngine.getInstance().search( queryString, startIndex, resultCount );
			
			result.setRequestTime( System.currentTimeMillis() - startTime );
			
			return result;
		}
		catch( EngineException ee )
		{
			return new SearchResult( false, ee.toString() );
		}
	}
	
	/** Performs a search and returns the approximate hit count. */
	public int guessHitCount( java.lang.String queryString ) {
		try
		{
			return LuceneEngine.getInstance().guessHitCount( queryString );
		}
		catch( EngineException ee )
		{
			return -1;
		}
	}
	
	/** Performs a search and returns the matching terms. */
	public java.lang.String[] retrieveMatchingTerms( java.lang.String queryString ) {
		try
		{
			return LuceneEngine.getInstance().retrieveMatchingTerms( queryString );
		}
		catch( EngineException ee )
		{
			return null;
		}
	}
}
