/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
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

	// Corresponds to 2000-01-01T00:00:00+00:00
	private static long MIN_EPOCH_SECONDS = 946684800L ;
	private static long MAX_EPOCH_SECONDS = 9999999999L ;
	
	public TimeStamp(ZonedDateTime instant) {
		instant = instant.withZoneSameInstant(ZoneOffset.UTC);
		if(!isValidInstant(instant)) {		
			throw new RNException(String.format("Illegal instant year: %d is not in the range %d : %d",
								                instant.toEpochSecond(), MIN_EPOCH_SECONDS, MAX_EPOCH_SECONDS)
		    );
		}
		this.instant =  instant;
	}

	public static boolean isValidInstant(ZonedDateTime instant) {
		long seconds = instant.toEpochSecond();
		return MIN_EPOCH_SECONDS <= seconds && seconds <= MAX_EPOCH_SECONDS;
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
