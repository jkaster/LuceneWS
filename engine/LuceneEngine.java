package com.codegear.dn.search.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;

/** LuceneEngine performs the actual indexing and searching of the index. */
public class LuceneEngine {
	/** Maximum number of single-word hints to return. */
	public static final int OneWordMax = 3;
	
	/** Maximum number of two-word hints to return. */
	public static final int TwoWordMax = 3;
	
	/** Maximum number of three-word hints to return. */
	public static final int ThreeWordMax = 3;
	
	/** The instance of the engine. */
	private static LuceneEngine instance = null;
	
	/** The location of the index. */
	private String indexDir = null;
	
	/** The searcher provider instance. */
	private SearcherProvider searcherProvider = null;
	
	public LuceneEngine() {
		// Debug logging, for test purposes, probably could be removed.
		Log.info( "LuceneEngine starting..." );
		
		indexDir = Options.getInstance().getIndexDirectory();
		if( indexDir == null )
			Log.error( "Index directory is null." );
		
		initProviders();
		
		// Initialize the indexer instance.
		LuceneIndexer.createInstance( indexDir );
		
		// Initialize the searcher provider.
		searcherProvider = new SearcherProvider( indexDir );
		searcherProvider.start();
	}
	
	public synchronized static LuceneEngine getInstance() {
		if( instance == null )
			instance = new LuceneEngine();
		
		return instance;
	}
	
	/** Index the content in a certain predefined location given the content's unique ID. */
	public void indexContentRecord( long contentID, String contentProviderID )
			throws EngineException {
		ContentFields fields = getContentFieldsFromProvider( contentProviderID, contentID );
		
		indexContent( fields );
	}
	
	/** Index content passed in as the fields parameter. */
	public void indexContent( ContentFields fields ) throws EngineException {
		// We merely pass the request onto the LuceneIndexer instance, so that
		// the indexing operation can be queued and processed.
		LuceneIndexer.getInstance().indexContent( fields );
	}
	
	/** Index HTML content passed in as the fields parameter. */
	public void indexHTMLContent( ContentFields fields ) throws EngineException {
		// First strip the HTML tags from the body of the document, then pass
		// it onto the regular index method.
		fields.convertHTMLToText();
		
		indexContent( fields );
	}
	
	/** Deletes a specified content ID from the index. */
	public void deleteContent( String appID, String contentID ) throws EngineException  {
		LuceneIndexer.getInstance().deleteContent( appID, contentID ); 
	}
	
	/** Perform a search on the index, returning the results found. */
	public SearchResult search( String queryString, int startIndex, int resultCount )
			throws EngineException {
		try
		{
			// Retrieve the searcher instance.
			Searcher searcher = searcherProvider.getSearcher();
			
			// Create the analyzer and parser.
		    Analyzer analyzer = new StandardAnalyzer();
		    MultiFieldQueryParser parser = new MultiFieldQueryParser( ContentFields.queryFields, analyzer );
		    Query queryInst;
		    Result[] results;
		    int totalHitCount;
		    
		    // Parse the string query. 
		    queryInst = parser.parse( queryString );
		    
		    // Perform the actual search.
		    if(resultCount >= 0)
		    {
			    Hits hits = searcher.search( queryInst );
			    if( resultCount == 0 )
			    	resultCount = hits.length();
			    
			    int hitCount = Math.min( resultCount, hits.length() - startIndex );
			    
			    totalHitCount = hits.length();
			    
			    results = new Result[ hitCount ];
			    
			    // Now loop through the desired result range, creating the SearchResult set.
			    for( int i = 0; (i < resultCount) && ((i + startIndex) < hits.length()); ++i )
			    	results[ i ] = new Result( hits.doc( i + startIndex ), hits.score( i + startIndex ) );
		    }
		    else
		    {
		    	ArrayList<Integer> docIDList = new ArrayList<Integer>( 1000 );
		    	ArrayList<Float> scoreList = new ArrayList<Float>( 1000 );
		    	MaxScoreHitCollector collector = new MaxScoreHitCollector( docIDList, scoreList );
		    	
			    searcher.search( queryInst, collector );
			    
			    // Process the results, retrieving the content ID for each item.
			    totalHitCount = docIDList.size();
			    
			    results = new Result[ docIDList.size() ];
			    
			    int index = 0;
			    float scoreNorm = 1.0f;
			    float maxScore = collector.getMaxScore();
			    
			    if( totalHitCount > 0 && maxScore > 1.0f )
			        scoreNorm = 1.0f / maxScore;
			    
			    for( Integer i : docIDList )
			    {
			    	results[ index ] = new Result( searcher.doc( i ), scoreList.get( index ) * scoreNorm );
			    	++index;
			    }
		    }
		    	
		    // Now return the result set.
		    return new SearchResult( results, totalHitCount );
		}
		catch( Exception e )
		{
			// If an exception is thrown, return it as an EngineException.
			Log.error( "Exception thrown in search." , e );
			
			throw new EngineException( e.toString() );
		}
	}
	
	/** Perform a search on the index, returning the results found. */
	public int guessHitCount( String queryString ) throws EngineException { 
		try
		{
			// Retrieve the searcher instance.
			Searcher searcher = searcherProvider.getSearcher();
			
			// Create the analyzer and parser.
		    Analyzer analyzer = new StandardAnalyzer();
		    MultiFieldQueryParser parser = new MultiFieldQueryParser( ContentFields.queryFields, analyzer );
		    Query queryInst;
		    Hits hits;
		    
		    // Parse the string query. 
		    queryInst = parser.parse( queryString );
		    
		    // Perform the actual search.
		    hits = searcher.search( queryInst );
		    
		    // Now return the hit count.
		    return hits.length();
		}
		catch( Exception e )
		{
			// If an exception is thrown, return it as an EngineException.
			Log.error( "Exception thrown in search." , e );
			
			throw new EngineException( e.toString() );
		}
	}
	
	/** Performs a search and returns the matching terms. */
	public String[] retrieveMatchingTerms( String queryString ) throws EngineException {
		try
		{
			ArrayList<String> results = retrieveRelatedSearches( queryString );
		    
		    String[] resultArray = new String[ results.size() ];
		    
		    return results.toArray( resultArray );
		}
		catch( Exception e )
		{
			// If an exception is thrown, return it as an EngineException.
			Log.error( "Exception thrown in search." , e );
			
			throw new EngineException( e.toString() );
		}
	}
	
	private void initProviders() {
	}
	
	/** Generates a valid ContentFields instance from the content in a certain predefined
	 * location given the content's unique ID. . */
	private ContentFields getContentFieldsFromProvider( String contentProviderID, long contentID )
			throws ProviderNotFoundException {
		//TODO: implement this.
		throw new ProviderNotFoundException( "Not implemented." );
	}
	
	/** Retrieves the hint list for a given input string.
	 * 
	 * For now, we're only pulling phrases from the body of the indexed
	 * content. 
	 * */
	private ArrayList<String> retrieveRelatedSearches(String query)
			throws CorruptIndexException, IOException, ParseException {
		Searcher searcher = searcherProvider.getSearcher();
		IndexReader reader = searcherProvider.getReader();
		
		Analyzer analyzer = new StandardAnalyzer();
	    MultiFieldQueryParser parser = new MultiFieldQueryParser( new String[] { "body" }, analyzer );
	    Query queryInst;

	    query = query.trim();
	    
	    // Validate the query string.
	    if( !query.matches( "[a-zA-Z0-9]+" ) )
	    	return null;
	    
	    // Parse the string query. 
	    queryInst = parser.parse( query + "*" );
	    
	    // Rewrite the query into a basic form.
	    queryInst = queryInst.rewrite( reader );

	    // Retrieve the terms of the query.
	    Set<Term> terms = new LinkedHashSet<Term>();
	    
	    queryInst.extractTerms(terms);

	    ArrayList<String> oneWordPhrases = new ArrayList<String>();
	    ArrayList<String> twoWordPhrases = new ArrayList<String>();
		ArrayList<String> threeWordPhrases = new ArrayList<String>();
	    
	    // Now iterate through all the terms, retrieving the text for each term.
	    for( Term t : terms )
	    {
	    	String term = t.text();
	    	
	    	if( oneWordPhrases.size() < LuceneEngine.OneWordMax )
	    		oneWordPhrases.add( "+" + term );
	    	
	    	if( twoWordPhrases.size() < LuceneEngine.TwoWordMax || threeWordPhrases.size() < LuceneEngine.ThreeWordMax )
	    	{
	    		Hits hits = retrieveHitsForTerm( searcher, term );
		
	    		for( int i = 0;
	    			 i < hits.length() &&
	    			 ( twoWordPhrases.size() < LuceneEngine.TwoWordMax ||
	    			   threeWordPhrases.size() < LuceneEngine.ThreeWordMax );
	    			 ++i )
	    		{
	    			buildPhrasesFromDoc( reader, hits.id( i ), term, twoWordPhrases, threeWordPhrases );
	    		}
	    	}
	    	
	    	if( oneWordPhrases.size() == LuceneEngine.OneWordMax &&
	    		twoWordPhrases.size() == LuceneEngine.TwoWordMax &&
	    		threeWordPhrases.size() == LuceneEngine.ThreeWordMax )
	    	{
	    		break;
	    	}
	    }
		
		oneWordPhrases.addAll( twoWordPhrases );
		oneWordPhrases.addAll( threeWordPhrases );
		
		return oneWordPhrases;
	}
	
	/** Takes a given document number, and extracts the 2 and 3 word phrases that start with term. */
	private void buildPhrasesFromDoc(IndexReader reader, int docNum, String term, ArrayList<String> twoWordPhrases,
				ArrayList<String> threeWordPhrases) throws IOException {
		TermFreqVector vector = reader.getTermFreqVector( docNum, "body" );
    	if( !(vector instanceof TermPositionVector) )
    		return;
    	
    	HashMap<Integer, ThreeWordResult> threeWordPieces = new HashMap<Integer, ThreeWordResult>();
    	
		TermPositionVector pVector = (TermPositionVector)vector;
		int wordPos = vector.indexOf( term );
    	if( wordPos > -1 )
    	{
    		int[] wordPositions = pVector.getTermPositions( wordPos );
    		String[] words = pVector.getTerms();
    		
    		HashMap<Integer, Integer> wordPositionsMap = new HashMap<Integer, Integer>();
    		for( int i = 0; i < wordPositions.length; ++i )
    			wordPositionsMap.put( wordPositions[ i ], i );
    		
    		for( int i = 0; i < pVector.size(); ++i )
    		{
    			int[] curWordPositions = pVector.getTermPositions( i );
    			
    			for( int j = 0; j < curWordPositions.length; ++j )
    			{
    				int curWordPosition = curWordPositions[ j ];
    				int prevWordPosition = curWordPosition - 1;
    				int prevPrevWordPosition = curWordPosition - 2;
    				int firstWordPosition = -1;
    				Integer k;
    				
    				if( ( k = wordPositionsMap.get( prevWordPosition ) ) != null )
    					firstWordPosition = prevWordPosition;
    				else if( ( k = wordPositionsMap.get( prevPrevWordPosition ) ) != null )
    					firstWordPosition = prevPrevWordPosition;
    				
    				if( firstWordPosition > -1 )
    				{
						if( prevWordPosition == firstWordPosition )
						{
							if( threeWordPieces.containsKey( k ) )
								threeWordPieces.get( k ).secondWord = words[ i ];
							else
								threeWordPieces.put( k, new ThreeWordResult( term, words[ i ], "" ) );
						}
						else if( prevPrevWordPosition == firstWordPosition )
						{
							if( threeWordPieces.containsKey( k ) )
								threeWordPieces.get( k ).thirdWord = words[ i ];
							else
								threeWordPieces.put( k, new ThreeWordResult( term, "", words[ i ] ) );
						}
    				}
    			}
    		}
    	}
    	
    	Collection<ThreeWordResult> values = threeWordPieces.values();
    	
    	for( ThreeWordResult r : values )
    	{
    		if( twoWordPhrases.size() < LuceneEngine.TwoWordMax &&
    			( r.thirdWord.length() == 0 || ( r.secondWord.length() > 0 && threeWordPhrases.size() == LuceneEngine.ThreeWordMax ) ) )
    		{
    			twoWordPhrases.add( "+" + r.firstWord + " +" + r.secondWord );
    		}
    		else if( threeWordPhrases.size() < LuceneEngine.ThreeWordMax && r.secondWord.length() > 0 && r.thirdWord.length() > 0 )
    		{
    			threeWordPhrases.add( "+" + r.firstWord + " +" + r.secondWord + " +" + r.thirdWord );
    		}
    	}
	}
	
	/** Retrieves the documents that contain term. */
	private Hits retrieveHitsForTerm(Searcher searcher, String term)
			throws CorruptIndexException, IOException {
	    return searcher.search( new TermQuery( new Term( "body", term ) ) );
	}
}

/** Simple structure for storing 3 strings. */
class ThreeWordResult
{
	public String firstWord;
	public String secondWord;
	public String thirdWord;
	
	public ThreeWordResult() {
	}
	
	public ThreeWordResult( String firstWord, String secondWord, String thirdWord ) {
		this.firstWord = firstWord;
		this.secondWord = secondWord;
		this.thirdWord = thirdWord;
	}
}

/** Custom HitCollector implementation to provide maxScore property. */
class MaxScoreHitCollector extends HitCollector {
	private ArrayList<Integer> docIDList = null;
	private ArrayList<Float> scoreList = null;

	private float maxScore = 0.0f;
	
	public MaxScoreHitCollector(ArrayList<Integer> docIDList, ArrayList<Float> scoreList) {
		this.docIDList = docIDList;
		this.scoreList = scoreList;
	}
	
	public void collect(int doc, float score) {
		if(score > 0.0f)
		{
			docIDList.add( doc );
			scoreList.add( score );
    	
			if(score > maxScore)
				maxScore = score;
		}
	}
	
	public float getMaxScore() {
		return maxScore;
	}
}