package com.codegear.dn.search.engine;


/** Encapsulates a search result set. */
public class SearchResult {

	private String errorMessage = "";
	private boolean error = false;
	private Result[] items = null;
	private long requestTime = 0;
	private int hitCount = 0;

	public SearchResult() {
		errorMessage = null;
		error = false;
		items = null;
	}
	
	public SearchResult( boolean error, String errorMessage ) {
		this.error = true;
		this.errorMessage = errorMessage;
	}
	
	public SearchResult( Result[] items, int hitCount ) {
		this.items = items;
		this.hitCount = hitCount;
	}
	
	public boolean getError() {
		return error;
	}

	public void setError(boolean property1) {
		this.error = property1;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String property1) {
		this.errorMessage = property1;
	}
	
	public Result[] getItems() {
		return items;
	}
	
	public void setItems( Result[] property1 ) {
		this.items = property1;
	}
	
	public long getRequestTime() {
		return requestTime;
	}
	
	public void setRequestTime( long property1 ) {
		requestTime = property1;
	}
	
	public int getHitCount() {
		return hitCount;
	}
}
