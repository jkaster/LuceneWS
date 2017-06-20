package com.codegear.dn.search.engine;

import org.apache.lucene.document.Document;

/** Contains the information for an individual search result. See ContentFields to see
 * which fields are stored by Lucene */
public class Result {
	private String appID;
	private String contentID;
	private String author = "";
	private String title = "";
	private String publicationDate = "";
	private String languageCode = "";
	private String product = "";
	private String version = "";
	private String extraData = "";
	private String contentType = "";
	private float score;
	
	public Result() {
		appID = null;
		contentID = null;
		author = null;
		title = null;
		publicationDate = null;
		languageCode = null;
		product = null;
		version = null;
		extraData = null;
		contentType = null;
		score = 0;
	}
	
	/** Creates the search result from the specified Lucene document. */
	public Result( Document document, float score ) {
		splitLuceneID( document.get( ContentFields.ID_FIELD ) );
		
		// Yorai doesn't need the following fields for GP results.
		if(!appID.equals("gp"))
		{
			author = document.get( ContentFields.AUTHOR_FIELD );
			title = document.get( ContentFields.TITLE_FIELD );
			publicationDate = document.get( ContentFields.PUBDATE_FIELD );
			languageCode = document.get( ContentFields.LANG_FIELD );
			product = document.get( ContentFields.PRODUCT_FIELD );
			version = document.get( ContentFields.VERSION_FIELD );
			extraData = document.get( ContentFields.EXTRADATA_FIELD );
			contentType = document.get( ContentFields.CONTENTTYPE_FIELD );
		}
		
		this.score = score;
	}
	
	public String getAppID() {
		return appID;
	}
	
	public String getContentID() {
		return contentID;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getPublicationDate() {
		return publicationDate;
	}
	
	public String getLanguageCode() {
		return languageCode;
	}
	
	public String getProduct() {
		return product;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getExtraData() {
		return extraData;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public float getScore() {
		return score;
	}
	
	/** Splits the LuceneID into the separate appID and contentID pieces. */
	private void splitLuceneID( String luceneID ) {
		int i = luceneID.indexOf( "." );
		
		// Should never ever happen, but if it does, we'll just return a bad index.
		if( i < 1 || i >= ( luceneID.length() - 1 ) )
		{
			this.appID = "";
			this.contentID = "";
		}
		
		this.appID = luceneID.substring( 0, i );
		this.contentID = luceneID.substring( i + 1 );
	}
}
