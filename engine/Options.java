package com.codegear.dn.search.engine;

import java.io.InputStream;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/** Retrieve and caches the options for the search service. */
public class Options {
	private static Options instance = null;
	
	private String indexDirectory = null;
	
	private Options() {
		loadOptions();
	}
	
	/** Returns a  */
	public static Options getInstance() {
		if( instance == null )
			instance = new Options();
		
		return instance;
	}

	public String getIndexDirectory() {
		return indexDirectory;
	}
	
	private void loadOptions() {
		InputStream optionsStream = loadResource( "/search_config.xml" );
		
		// Verify that we have the XML file.
		if( optionsStream == null )
		{
			Log.error( "Cannot find config file in class path." );
			return;
		}
		
		DOMParser parser = new DOMParser();
		try
		{
			parser.parse( new InputSource( optionsStream ) );
		}
		catch( Exception e )
		{
			Log.error( "Error while loading options", e ); 
			return;
		}
		
		Document document = parser.getDocument();		
		Element rootNode = document.getDocumentElement();
		
		// Find the options node.
		NodeList childNodes = rootNode.getElementsByTagName( "options" );
		if( childNodes.getLength() > 0 )
		{
			Node optionsNode = childNodes.item( 0 );
			
			// Loop through the options.
			for( Node childNode = optionsNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling() )
			{
				if( childNode.getNodeType() == Node.ELEMENT_NODE )
				{
					if( childNode.getNodeName().equals( "indexdir" ) )
						indexDirectory = childNode.getTextContent();
				}
			}
		}
	}
	
	private InputStream loadResource(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        if( in == null )
        {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
            if( in == null )
                in = getClass().getClassLoader().getResourceAsStream(name);
        }
        
        return in;
    }
}
