/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
package uk.gov.food.rn;

public class RNException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3337530546179420650L;
	
	/**
	 * 
	 */
	public RNException() {
		super();
	}
	
	public RNException(String msg) {
		super(msg);
	}
	
	public RNException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RNException(Throwable cause) {
		super(cause);
	}
	
	public RNException(String msg, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(msg,cause, enableSuppression, writableStackTrace);
	}
}
 
