/******************************************************************
 * File:        TimeStamp.java   
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  9 Apr 2018
 * 
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 *
 *****************************************************************/
package uk.gov.food.rn;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Encapuslates a limited date range instant (java.time.ZonedDateTime)
 * 
 * For FSA-RNs the corresponding instant must be between the start of 2000
 * and the end of 9999.
 *
 */
public class TimeStamp  {
	private ZonedDateTime instant;
	
	private static int MIN_INSTANT_YEAR = 2000 ;
	private static int MAX_INSTANT_YEAR = 9999 ;
	
	public TimeStamp(ZonedDateTime instant) {
		instant = instant.withZoneSameInstant(ZoneOffset.UTC);
		if(!isValidInstant(instant)) {		
			throw new RNException(String.format("Illegal instant year: %d is not in the range %d : %d",
								                instant.getYear(), MIN_INSTANT_YEAR, MAX_INSTANT_YEAR)
		    );
		}
		this.instant =  instant;
	}

	public static boolean isValidInstant(ZonedDateTime instant) {
		int year = instant.getYear();
		return MIN_INSTANT_YEAR <= year && year <= MAX_INSTANT_YEAR ;
	}
	
	/**
	 * @return the instant
	 */
	public  ZonedDateTime getInstant() {
		return instant;
	}
	
	/**
	 * @param other
	 * @return
	 */
	public boolean equals(TimeStamp other) {
		return instant.equals(other.instant);
	}
	
	public int hashCode() {	
		return instant.hashCode();
	}
}
