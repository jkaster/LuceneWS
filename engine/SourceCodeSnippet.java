package com.codegear.dn.search.engine;

public class SourceCodeSnippet {
	private String comments;
	private String language;
	private String source;
	private String sourceWithoutComments;
	private String sourceWithoutStrings;
	private String strings;

	/** Simple helper method to append another snippet to the end of this one. */
	public void appendSnippet( SourceCodeSnippet snippet ) {
		appendComments( snippet.getComments() );
		appendSource( snippet.getSource() );
		appendSourceWithoutComments( snippet.getSourceWithoutComments() );
		appendSourceWithoutStrings( snippet.getSourceWithoutStrings() );
		appendStrings( snippet.getStrings() );
	}
	
	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String property1) {
		this.language = property1;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String property1) {
		this.source = property1;
	}

	public String getSourceWithoutComments() {
		return sourceWithoutComments;
	}

	public void setSourceWithoutComments(String property1) {
		this.sourceWithoutComments = property1;
	}
	
	public String getSourceWithoutStrings() {
		return sourceWithoutStrings;
	}

	public void setSourceWithoutStrings(String property1) {
		this.sourceWithoutStrings = property1;
	}

	public String getStrings() {
		return strings;
	}

	public void setStrings(String property1) {
		this.strings = property1;
	}
	
	/** Simple helper method to merge snippet comments. */
	private void appendComments(String comments) {
		this.comments += " " + comments;
	}
	
	/** Simple helper method to merge snippet source. */
	private void appendSource(String source) {
		this.source += " " + source;
	}
	
	/** Simple helper method to merge snippet source that contains comments. */
	private void appendSourceWithoutComments(String sourceWithoutComments) {
		this.sourceWithoutComments += " " + sourceWithoutComments;
	}
	
	/** Simple helper method to merge snippet source that doesn't contain strings. */
	private void appendSourceWithoutStrings(String sourceWithoutStrings) {
		this.sourceWithoutStrings += " " + sourceWithoutStrings;
	}
	
	/** Simple helper method to merge snippet strings. */
	private void appendStrings(String strings) {
		this.strings += " " + strings;
	}
}
