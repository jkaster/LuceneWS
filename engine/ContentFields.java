package com.codegear.dn.search.engine;

import java.util.HashMap;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class ContentFields {
	/** Predefined Lucene field names */
	public static final String APPID_FIELD = "appid";
	public static final String ID_FIELD = "id";
	public static final String AUTHOR_FIELD = "author";
	public static final String TITLE_FIELD = "title";
	public static final String SUMMARY_FIELD = "summary";
	public static final String BODY_FIELD = "body";
	public static final String PUBDATE_FIELD = "pubdate";
	public static final String LANG_FIELD = "language";
	public static final String COMMENTS_FIELD = "comments";
	public static final String PRODUCT_FIELD = "product";
	public static final String VERSION_FIELD = "version";
	public static final String TAGS_FIELD = "tags";
	public static final String CATEGORY_FIELD = "category";
	public static final String EXTRADATA_FIELD = "extradata";
	public static final String CONTENTTYPE_FIELD = "contenttype";
	public static final String WORKAROUND_FIELD = "workaround";
	public static final String ALLSOURCE_FIELD = "source";
	
	/** Contains the field names that should be searched without an explicit field specifier.
	 *  These fields should also have their term vector stored. */
	public static final String[] queryFields = new String[] { TITLE_FIELD, SUMMARY_FIELD, BODY_FIELD };
	
	private String appID = "";
	private String contentID = "";
	private String author = "";
	private String title = "";
	private String summary = "";
	private String body = "";
	private String publicationDate = "";
	private String languageCode = "";
	private String comments = "";
	private String product = "";
	private String version = "";
	private String tags = "";
	private String category = "";
	private String extraData = "";
	private String contentType = "";
	private String workaround = "";
	private String allSource = "";
	private SourceCodeSnippet[] snippets = null;
	
	public ContentFields( String appID, String contentID, String author, String title,
			String summary, String body, String publicationDate, String languageCode )
			throws EngineException {
		if( appID != null )
			this.appID = appID;
		
		if( contentID != null )
			this.contentID = contentID;
		
		if( author != null )
			this.author = author;
		
		if( title != null )
			this.title = title;
		
		if( summary != null )
			this.summary = summary;
		
		if( body != null )
			this.body = body;
		
		if( publicationDate != null )
			this.publicationDate = publicationDate;
		
		if( languageCode != null )
			this.languageCode = languageCode;
		
		validateID();
		validateDate();
	}
	
	public ContentFields( String appID, String contentID, String author, String title,
			String summary, String body, String publicationDate, String languageCode,
			String comments, String product, String version, String tags, String category,
			String extraData, String contentType, String workaround, boolean isHTML ) throws EngineException {
		if( appID != null )
			this.appID = appID;

		if( contentID != null )
			this.contentID = contentID;

		if( author != null )
			this.author = author;

		if( title != null )
			this.title = title;

		if( summary != null )
			this.summary = summary;

		if( body != null )
			this.body = body;

		if( publicationDate != null )
			this.publicationDate = publicationDate;

		if( languageCode != null )
			this.languageCode = languageCode;

		if( comments != null )
			this.comments = comments;

		if( product != null )
			this.product = product;

		if( version != null )
			this.version = version;

		if( tags != null )
			this.tags = tags;

		if( category != null )
			this.category = category;

		if( extraData != null )
			this.extraData = extraData;

		if( contentType != null )
			this.contentType = contentType;

		if( workaround != null )
			this.workaround = workaround;
		
		validateID();
		validateDate();
		
		if( isHTML )
			convertHTMLToText();
	}
	
	public ContentFields( String appID, String contentID, String author, String title,
			String summary, String body, String publicationDate, String languageCode,
			String comments, String product, String version, String tags, String category,
			String extraData, String contentType, String allSource, SourceCodeSnippet[] snippets,
			boolean isHTML ) throws EngineException {

		if( appID != null )
			this.appID = appID;

		if( contentID != null )
			this.contentID = contentID;

		if( author != null )
			this.author = author;

		if( title != null )
			this.title = title;

		if( summary != null )
			this.summary = summary;

		if( body != null )
			this.body = body;

		if( publicationDate != null )
			this.publicationDate = publicationDate;

		if( languageCode != null )
			this.languageCode = languageCode;

		if( comments != null )
			this.comments = comments;

		if( product != null )
			this.product = product;

		if( version != null )
			this.version = version;

		if( tags != null )
			this.tags = tags;

		if( category != null )
			this.category = category;

		if( extraData != null )
			this.extraData = extraData;

		if( contentType != null )
			this.contentType = contentType;

		if( allSource != null )
			this.allSource = allSource;

		this.snippets = snippets;
		
		validateID();
		validateDate();
		validateSnippets();
		
		if( isHTML )
			convertHTMLToText();
	}
	
	/** Retrieve the unique Lucene ID */
	public String getLuceneID() {
		// return this.appID + "." + this.contentID;
		return generateLuceneID( this.appID, this.contentID );
	}
	
	/** Generate a Lucene ID from a specified appID and contentID. */
	public static String generateLuceneID( String appID, String contentID ) {
		return appID + "." + contentID;
	}
	
	/** Convert the data in the fields into a Lucene Document instance. */
	public Document asDocument() {
	    Document doc = new Document();

	    // Unique Lucene ID is the appID merged with the contentID.
	    doc.add( new Field( ID_FIELD, getLuceneID(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
	    
	    // Fields whose content is stored.
	    addField( doc, AUTHOR_FIELD, this.author, Field.Store.YES, Field.Index.TOKENIZED );
	    addField( doc, TITLE_FIELD, this.title, Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.WITH_POSITIONS );
	    addField( doc, PUBDATE_FIELD, this.publicationDate, Field.Store.YES, Field.Index.UN_TOKENIZED );
	    addField( doc, LANG_FIELD, this.languageCode, Field.Store.YES, Field.Index.UN_TOKENIZED );
	    addField( doc, PRODUCT_FIELD, this.product, Field.Store.YES, Field.Index.UN_TOKENIZED );
	    addField( doc, VERSION_FIELD, this.version, Field.Store.YES, Field.Index.UN_TOKENIZED );
	    addField( doc, EXTRADATA_FIELD, this.extraData, Field.Store.YES, Field.Index.NO );
	    addField( doc, CONTENTTYPE_FIELD, this.contentType, Field.Store.YES, Field.Index.NO );
	    
	    // Fields whose content is not stored.
	    addField( doc, APPID_FIELD, this.appID, Field.Store.NO, Field.Index.UN_TOKENIZED );
	    addField( doc, SUMMARY_FIELD, this.summary, Field.Store.NO, Field.Index.TOKENIZED );
	    addField( doc, BODY_FIELD, this.body, Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.WITH_POSITIONS );
	    addField( doc, COMMENTS_FIELD, this.comments, Field.Store.NO, Field.Index.TOKENIZED );
	    addField( doc, TAGS_FIELD, this.tags, Field.Store.NO, Field.Index.TOKENIZED );
	    addField( doc, CATEGORY_FIELD, this.category, Field.Store.NO, Field.Index.UN_TOKENIZED );
	    addField( doc, WORKAROUND_FIELD, this.workaround, Field.Store.NO, Field.Index.TOKENIZED );
	    addField( doc, ALLSOURCE_FIELD, this.allSource, Field.Store.NO, Field.Index.TOKENIZED );
	    
	    // Source code snippets.
	    if( this.snippets != null && this.snippets.length > 0 )
	    {
	    	HashMap<String, SourceCodeSnippet> snippetMap = new HashMap<String, SourceCodeSnippet>();
	    	SourceCodeSnippet temp;
	    	
	    	// First merge all the snippets together by language.
	    	for( SourceCodeSnippet snippet : snippets )
	    	{
	    		 temp = snippetMap.get( snippet.getLanguage() );
	    		 if( temp == null )
	    		 {
	    			 temp = new SourceCodeSnippet();
	    			 temp.setLanguage( snippet.getLanguage() );
	    			 snippetMap.put( snippet.getLanguage(), temp );
	    		 }
	    		 
	    		 temp.appendSnippet( snippet );
	    	}
	    	
	    	// Now add the fields to the Lucene document.
	    	Set<String> keys = snippetMap.keySet();
	    	
	    	for( String snippetLanguage : keys )
	    	{
	    		temp = snippetMap.get( snippetLanguage );
	    		if( temp == null )
	    			continue;
	    		
	    		addField( doc, snippetLanguage + ".comments", temp.getComments(), Field.Store.NO, Field.Index.TOKENIZED );
	    		addField( doc, snippetLanguage + ".source", temp.getSource(), Field.Store.NO, Field.Index.TOKENIZED );
	    		addField( doc, snippetLanguage + ".sourceWithoutComments", temp.getSourceWithoutComments(), Field.Store.NO, Field.Index.TOKENIZED );
	    		addField( doc, snippetLanguage + ".sourceWithoutStrings", temp.getSourceWithoutStrings(), Field.Store.NO, Field.Index.TOKENIZED );
	    		addField( doc, snippetLanguage + ".strings", temp.getStrings(), Field.Store.NO, Field.Index.TOKENIZED );
	    	}
	    }
	    
	    return doc;
	}
	
	/** Convert the body from HTML to plain text. */
	public void convertHTMLToText() {
		this.body = processEntities( this.body.replaceAll( "<\\/?[a-zA-Z][^>]*>", "" ) );
		this.comments = processEntities( this.comments.replaceAll( "<\\/?[a-zA-Z][^>]*>", "" ) );
	}
	
	/** Validate the date format. */
	private void validateDate() throws EngineException {
		if( this.publicationDate.length() > 0 && !this.publicationDate.matches( "[0-9]{4}[0-9]{2}[0-9]{2}([0-9]{2}[0-9]{2}[0-9]{2})?" ) )
			throw new EngineException( "Date format '" + this.publicationDate + "' incorrect. Required format yyyymmdd[hhmmss]." );
	}
	
	/** Validate the content ID */
	private void validateID() throws EngineException {
		if( this.appID.length() == 0 || this.contentID.length() == 0 )
			throw new EngineException( "appID or contentID is empty, cannot create index for invalid ID." );
	}
	
	/** Validate the code snippets passed in for this document. **/
	private void validateSnippets() throws EngineException {
		if( snippets != null )
		{
			for( SourceCodeSnippet snippet : snippets )
			{
				if( snippet.getLanguage() == null || snippet.getLanguage().trim().length() == 0 )
					throw new EngineException( "snippet language cannot be empty." );
				
				if( !snippet.getLanguage().matches( "[a-zA-Z0-9]+" ) )
					throw new EngineException( "snippet language can only contain letters and numbers." );
			}
		}
	}
	
	/** Convert or remove all HTML entities */
	private String processEntities( String html ) {
		html = html.replaceAll( "&nbsp;", " " );
		html = html.replaceAll( "&#160;", " " );
		html = html.replaceAll( "&quot;", "\"" );
		html = html.replaceAll( "&((#[0-9]+)|([a-z]+));", " " );
		
		return html;
	}
	
	/** Only adds fields to the document that aren't empty. */
	private void addField( Document doc, String fieldName, String fieldValue, Field.Store store, Field.Index index ) {
		if( fieldValue != null && fieldValue.length() > 0 )
			doc.add( new Field( fieldName, fieldValue, store, index ) ); 
	}
	
	private void addField( Document doc, String fieldName, String fieldValue, Field.Store store, Field.Index index, Field.TermVector vector ) {
		if( fieldValue != null && fieldValue.length() > 0 )
			doc.add( new Field( fieldName, fieldValue, store, index, vector ) );
	}
}
