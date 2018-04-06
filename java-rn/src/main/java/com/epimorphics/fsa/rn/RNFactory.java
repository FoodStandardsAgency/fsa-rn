/******************************************************************
 * File:        RNFactory.java
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  3 Apr 2018
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

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RNFactory  {
	private final int authority  ;
	private final int instance ;
	private final int type       ;
	private long prev = 0 ;
	
	
	/*
	 *  Use a Map to ensure there is a single factory instance for each authority,instance and type combination
	 *  within the same JVM.
	 *  
	 */
	
	private static HashMap<Integer,RNFactory> factories = new HashMap<Integer,RNFactory>() ;
	
	public static RNFactory getFactory(int authority, int instance, int type) throws RNException {
		checkFactoryFields(authority, instance, type);
		int key = (((authority*10)+instance)*100)+type;
		RNFactory res = null;
		
		synchronized (factories) {
			res = factories.get(key);
		   
		    if(res == null) {
			   res = new RNFactory(authority, instance, type);
			   factories.put(key,res);
		    }
	      	return res;
		}
	}
	
	
	private RNFactory(int authority, int instance, int type) throws RNException {
		this.authority = authority;
		this.instance = instance;
		this.type      = type;
	}
	
	public RN generateReferenceNumber() {
		// Make sure at least one millisecond has elapse since the last reference number was generated
		long time = 0;
		synchronized(this) {
			time = System.currentTimeMillis();
			while (time <= prev) {
				try {
					TimeUnit.MILLISECONDS.sleep(1);
				} catch (InterruptedException e) {
					// Ignore and go round again if needs be.
				}
				time = System.currentTimeMillis();
			}
			prev = time;
		}
		Instant instant = Instant.ofEpochMilli(time);
		LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		
		long year  = ChronoField.YEAR.getFrom(ldt);
		long month = ChronoField.MONTH_OF_YEAR.getFrom(ldt);
		long day   = ChronoField.DAY_OF_MONTH.getFrom(ldt);
		long hour  = ChronoField.HOUR_OF_DAY.getFrom(ldt);
		long min   = ChronoField.MINUTE_OF_HOUR.getFrom(ldt);
		long sec   = ChronoField.SECOND_OF_MINUTE.getFrom(ldt);
		long milli = ChronoField.MILLI_OF_SECOND.getFrom(ldt);
		
		String rrn_decimal = String.format("%03d%04d%01d%02d%04d%02d%02d%02d%02d%02d", milli, authority, instance, type, year, month, day, hour, min, sec);
		
		try {
			return new RN(new BigInteger(rrn_decimal));
		} catch (RNException e) {
			return null;
		}
	}
	
	private static void checkFactoryFields(int authority, int instance, int type) throws RNException  {
		if (authority<1000 || authority>9999 || instance>9 || type >99) {
			StringBuffer msg = new StringBuffer();
			
			if(authority<1000 || authority>9999)
				msg.append("Bad Authority Number (1000-9999): "+authority+ "; ");
			if(instance>9)
				msg.append("Bad instance number(0-9): "+instance+"; ");
			if(type>99)
				msg.append("Bad Reference Number Type (0-99): "+type+"; ");
			throw new RNException( msg.toString());
		}
	}
	
	public static void main(String[] args) {
		// Make factory
		RNFactory rnf = null;
		try {
			rnf = RNFactory.getFactory(1000, 1, 1);
		} catch (RNException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		}
		
		// Generate some RN's
		RN [] arr = new RN[100];
		for (int i = 0; i < 100; i++) {
			arr[i] = rnf.generateReferenceNumber();
		}

		// Print them out in various forms.
		for(int i = 0; i<100; i++) {
		   System.out.println( arr[i].getEncodedForm() + 
				         " " + arr[i].getDecimalForm()+
				         " " + arr[i].getDecodedDecimalForm() 
		   );
		}
		
		System.out.println();
		
		for(int i=0; i<100; i++) {
			String ef = arr[i].getEncodedForm();
			
			// Break the encoded form
			
			ef = ef.replaceAll("K","P");
			RN rn;
			if( (rn = RN.decode(ef)) == null)
				System.out.println("Broken RN: "+ef);
			else
				System.out.println(rn.getEncodedForm()+" "+rn.getDecimalForm()+" "+rn.getDecodedDecimalForm());
		}
	}
}
