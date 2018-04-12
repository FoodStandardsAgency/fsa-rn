/*******************************************************************************
 * File:        RNTest.java
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  4 Apr 2018
 *
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 *
 ******************************************************************************/
package uk.gov.food.rn;

import static org.junit.Assert.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import uk.gov.food.rn.Authority;
import uk.gov.food.rn.Instance;
import uk.gov.food.rn.RN;
import uk.gov.food.rn.RNException;
import uk.gov.food.rn.TimeStamp;
import uk.gov.food.rn.Type;

/**
 * Unit test for {@link RN}
 */
public class RNTest 
{ 
	@Test
	public void decodeLastRN() {
		RN rn;
		try {
			rn = new RN("SZVSDH-BQ8YGP-SJFWJ6");
			assertTrue("authority id should be 9999", rn.getAuthority().equals(new Authority("9999")));
			assertTrue("instance  id should be 9",    rn.getInstance().equals(new Instance("9")));
			assertTrue("type id should be 99",        rn.getType().equals(new Type("99")));
			assertTrue("timestamp should be 9999-12-31T23:59:59.999Z", 
					rn.getInstant().equals(new TimeStamp(ZonedDateTime.of(9999, 12, 31,23,59,59,999*1000000,ZoneOffset.UTC))) );
		} catch (Throwable e) {
			fail("Unexpected Throwable: "+e.getMessage());
		}	
	}
	
	@Test 
	public void detectCorruptRN() {
		try {
			RN rn = new RN("SZVSDH-BQ8YZP-SJFWJ6");
			assertTrue("constructor should have fired excetion",rn==null);
		} catch (RNException e) {
			// Expected
		}
	}
	
	@Test
	public void encodeRN() {
		try {
		assertTrue("", new RN(new Authority(1000),
				              new Instance(1),
				              new Type(21),
				              ZonedDateTime.of(2018,04,12,12,34,51,468*1000000,ZoneOffset.UTC))
						.getEncodedForm().equals("H41ZC8-20CPBR-0SDD9J"));
		} catch (Throwable e) {
			fail("Unexpected Throwable: "+ e.getMessage());
		}
	}
}
