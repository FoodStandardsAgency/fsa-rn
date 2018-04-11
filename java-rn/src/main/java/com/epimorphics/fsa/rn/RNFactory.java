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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RNFactory  {
	private final Authority authority  ;
	private final Instance  instance   ;
	private final Type      type       ;
	private long  prev = 0 ;
	
	
	public boolean equals(RNFactory other) {
		return authority.equals(other.authority) &&
               instance.equals(other.instance) &&
               type.equals(other.type);
	}
	
	@Override
	public int hashCode() {
		int res = authority.getId();		
		res     = instance.getId() + res*(Instance.MAX_INSTANCE_ID+1);
		res     = type.getId()     + res*(Type.MAX_TYPE_ID+1);	
		return res;
	}
	
	/*
	 *  Use a Map to ensure there is a single factory instance for each authority,instance and type combination
	 *  within the same JVM.
	 *  
	 */
	
	private static HashMap<Integer,RNFactory> factories = new HashMap<Integer,RNFactory>() ;
	
	public static RNFactory getFactory(Authority authority, Instance instance, Type type) throws RNException {
		RNFactory res = new RNFactory(authority, instance, type);
		
		synchronized (factories) {
			if (factories.containsKey(res.hashCode())) {
				res = factories.get(res.hashCode());
			} else {
				factories.put(res.hashCode(), res);
			}
		}
		
		return res;
	}	
	
	private RNFactory(Authority authority, Instance instance, Type type) throws RNException {
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
		
		
		ZonedDateTime instant = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time),ZoneOffset.UTC);

		try {
			return new RN(authority, instance, type, instant);
		} catch (RNException e) {
			return null;
		}
		
	}
	
	/** @@TODO Factor this main out into some JUnit tests */
	
	public static void main(String[] args) {
		// Make factory
		RNFactory rnf = null;
		RNFactory rnf2 = null;
		try {
			rnf =  RNFactory.getFactory(new Authority(1000), new Instance(1), new Type(21)) ;
			rnf2 = RNFactory.getFactory(new Authority(1000), new Instance(1), new Type(21)) ;
			
			if(rnf.equals(rnf2) && rnf != rnf2) {
				System.out.println("RNFs are strangely different");
			}
			
		} catch (RNException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		}
		
		// Generate some RN's
		RN [] arr = new RN[100];
		for (int i = 0; i < 99; i++) {
			arr[i] = rnf.generateReferenceNumber();
		}
		
		try {
			arr[99] = new RN( new BigInteger("999999999999991231235959"));
		} catch (RNException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
			try {
				rn = new RN(Codec.decode(ef));
				System.out.println(rn.getEncodedForm()+" "+rn.getDecimalForm()+" "+rn.getDecodedDecimalForm());			
			} catch (RNException e) {
			    System.out.println(e.getMessage());	
			}
		}
	}
}
