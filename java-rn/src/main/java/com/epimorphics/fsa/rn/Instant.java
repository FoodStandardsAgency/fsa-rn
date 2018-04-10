/******************************************************************
 * File:        Instant.java   
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  9 Apr 2018
 * 
 * Copyright (C) 2018 Food Standards Agency
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *****************************************************************/
package com.epimorphics.fsa.rn;

import java.time.ZonedDateTime;

/**
 * Encapuslates a limited date range instant (java.time.ZonedDateTime)
 * 
 * For FSA-RNs the corresponding instant must be between the start of 2000
 * and the end of 9999.
 *
 */
public class Instant  {
	private ZonedDateTime instant;
	
	private static int MIN_INSTANT_YEAR = 2000 ;
	private static int MAX_INSTANT_YEAR = 9999 ;
	
	Instant(ZonedDateTime instant) throws RNException {
		if(!isValidInstant(instant)) {		
			throw new RNException(String.format("Illegal instant year: %d is not in the range %d : %d",
								                instant.getYear(), MIN_INSTANT_YEAR, MAX_INSTANT_YEAR)
		    );
		}
		this.instant = instant;		
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
	public boolean equals(Instant other) {
		return instant.equals(other.getInstant());
	}
	
	public int hashCode() {
		return instant.hashCode();
	}
}
