/*******************************************************************************
 * File:        RN.java
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  5 Apr 2018
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
 ******************************************************************************/
package com.epimorphics.fsa.rn;

import java.math.BigInteger;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class RN implements Comparable<RN> {

    private Authority  authority;			// Issuing authority
    private Instance   instance  ;			// Deployed reference number generator service instance
    private Type       type ;		        // Reference number type
    private Instant    instant = null ;     // Time instant when issued (1ms precision)
    private String     rn_str = null ;      // External reference number string form (base 33 encoded integer with check digits)
    private BigInteger rn_int = null;       // Decoded reference number (decimal form integer) 


    /**
     * @return the authority
     */
    public Authority getAuthority() {
        return authority;
    }

    /**
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the instant
     */
    public ZonedDateTime getInstant() {
        return instant.getInstant();
    }

    public String toString() {
        return getEncodedForm();
    }
    
    
    /**
     * Construct an RN form its constituent parts.
     * 
     * May throw an RNException if the dateTime indicated by instant is outside the range permitted for RNs.
     * 
     * @param authority issuing authority
     * @param instance  issuing service instance
     * @param type      reference number type
     * @param instant   time instant when issued
     * @throws RNException
     */
    RN(Authority authority, Instance instance, Type type, ZonedDateTime instant) throws RNException {
    	this.authority = authority;
    	this.instance  = instance;
    	this.type      = type;
    	this.instant   = new Instant(instant);
    }

    /**
     * Constructs a new RN from a decimal form integer.
     * 
     * May throw an RNException if any of the embedded field values are outside their permitted ranges.
     *
     * @param decimalForm    A decimal form integer
     * @throws RNException
     */

    protected RN(BigInteger i) throws RNException {
        parseDecimalForm(i);
        rn_int = i;
    }


    /**
     * Construct an RN from its encoded form (base 33 encoded string with check digits).
     * 
     *  May throw an RNException if an if the embedded field values are outside their permitted ranges.
     * 
     * @param  encodedForm
     * @throws RNException
     */
    protected RN(String encodedForm) throws RNException  {
    	rn_int = Codec.decode(encodedForm);
        parseDecimalForm(rn_int);    	
    }

    /**
     * Parses the integer value of an RN's decimal form into 
     * its constituent Authority, Instance, Type and Instant fields.
     * 
     * @param decimal The decimal value to be parsed.
     * 
     */
    private void parseDecimalForm(BigInteger decimal) throws RNException {
        String i = String.format("%024d", decimal);
        if( i.length()!=24) {
            throw new RNException("Bad Decimal form (incorrect length): "+ i);
        }

        /*
         *          8       16  20
         *          |       |   |
         *  uuuaaaaittyyyyMMddhhmmss
         *  |  |   |  |   |   |   |
         *  0  3   7  10  14  18  22
         *
         */

        int sec   = Integer.parseInt(i.substring(22,24) );
        int min   = Integer.parseInt(i.substring(20,22));
        int hour  = Integer.parseInt(i.substring(18,20));
        int day   = Integer.parseInt(i.substring(16,18));
        int month = Integer.parseInt(i.substring(14,16));
        int year  = Integer.parseInt(i.substring(10,14));
        type      = new Type(i.substring(8,10));
        instance  = new Instance(i.substring(7,8));
        authority = new Authority(i.substring(3,7));
        int milli = Integer.parseInt(i.substring(0,3));

        try {
            instant = new Instant( ZonedDateTime.of(year, month, day, hour, min, sec, milli*1000000, ZoneOffset.UTC) );
        } catch (DateTimeException e) {
            throw new RNException("Bad Decimal form (illegal date): "+i,  e);
        }
    }

    /**
     * Returns the (Base 33) encoded form of a RN
     *
     * @return The encoded string form of an RN (including check digits).
     */
    public String getEncodedForm() {
    	if(rn_str == null) {
    		rn_int = new BigInteger(getDecimalForm());
    		rn_str = Codec.encode(rn_int);
    	}
        return rn_str;
    }

	/**
	 * @return
	 */
	protected String getDecimalForm() {
		ZonedDateTime timestamp = instant.getInstant();
		long year  = ChronoField.YEAR.getFrom(timestamp);
		long month = ChronoField.MONTH_OF_YEAR.getFrom(timestamp);
		long day   = ChronoField.DAY_OF_MONTH.getFrom(timestamp);
		long hour  = ChronoField.HOUR_OF_DAY.getFrom(timestamp);
		long min   = ChronoField.MINUTE_OF_HOUR.getFrom(timestamp);
		long sec   = ChronoField.SECOND_OF_MINUTE.getFrom(timestamp);
		long milli = ChronoField.MILLI_OF_SECOND.getFrom(timestamp);
		String decimalForm = String.format("%03d%04d%01d%02d%04d%02d%02d%02d%02d%02d", milli, authority.getId(), instance.getId(), type.getId(), year, month, day, hour, min, sec);
		return decimalForm;
	}

    /**
     * Return a fielded string form of the identifier that reveals its components
     *
     *   - authority
     *   - instance
     *   - type
     *   - instant (as a UTC 8601 dateTime string)
     *   
     *   aaaa:i:tt:yyyy-MM-ddThh:mm:ss.uuuZ
     *
     * @return
     */
    public String getDecodedDecimalForm() {
        return String.format("%04d",getAuthority().getId())+
        ":" + String.format("%01d",getInstance().getId())+
        ":" + String.format("%02d",getType().getId())+
        ":" + getInstant();
    }
    
    public boolean equals (RN other) {
    	return authority.equals(other.getAuthority()) &&
    		   instance.equals(other.getInstance()) &&
    		   type.equals(other.getType()) &&
    		   instant.equals(other.getInstant()) ;
    }

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * 
	 * Order by decoded decimal form
	 * 
	 * aaaa:i:tt:yyyy-MM-ddThh:mm:ss.uuuZ
	 */
    @Override
	public int compareTo(RN other){
    	return getDecodedDecimalForm().compareTo(other.getDecodedDecimalForm());
	}
    	
}
