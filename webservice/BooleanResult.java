package com.codegear.dn.search.webservice;

/** Encapsulates a boolean web service result, with an optional message. */
public class BooleanResult {

	private String message;
	private boolean result;

	public BooleanResult() {
		result = false;
		message = null;
	}
	
	public BooleanResult( boolean result, String message ) {
		this.result = result;
		this.message = message;
	}
	
	public boolean getResult() {
		return result;
	}

	public void setResult(boolean property1) {
		this.result = property1;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String property1) {
		this.message = property1;
	}
}
