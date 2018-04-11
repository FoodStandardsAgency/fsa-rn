package com.epimorphics.fsa.rn;

import static org.junit.Assert.*;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

/**
 * Unit tests on {@link TimeStamp}
 */
public class TimeStampTest {

    @Test
    public void shouldAllowLegalInstant() {
        assertTrue("should be a legal instant", TimeStamp.isValidInstant(ZonedDateTime.now(ZoneOffset.UTC)));
        assertTrue("should be a legal instant", TimeStamp.isValidInstant(ZonedDateTime.of(2018,01,01,00,00,00,0,ZoneOffset.UTC)));
    }

    @Test
    public void shouldNotAllowIllegalInstant() {
        assertFalse("should not be a legal instant", TimeStamp.isValidInstant(ZonedDateTime.of(1999,01,01,00,00,00,0,ZoneOffset.UTC)));
        assertFalse("should not be a legal instant", TimeStamp.isValidInstant(ZonedDateTime.of(10000,01,01,00,00,00,0,ZoneOffset.UTC)));
        try {
        	assertFalse("should not be a legal instant", TimeStamp.isValidInstant(ZonedDateTime.of(2018,02,29,00,00,00,0,ZoneOffset.UTC)));
        } catch (DateTimeException e) {
        	// Ignore (pass)
        }
    }

    @Test
    public void shouldConstructALegalInstant() {
        try {
            assertNotNull(new TimeStamp(ZonedDateTime.of(2018,01,01,00,00,00,0,ZoneOffset.UTC)));
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }

    @Test
    public void shouldNotConstructALegalInstant() {
        try {
            assertNotNull(new TimeStamp(ZonedDateTime.of(1999,01,01,00,00,00,0,ZoneOffset.UTC)));
            fail("should not be permitted");
        }
        catch (RNException e) {
        }
    }


    @Test
    public void shouldReturnTheInstant() {
        try {
            assertEquals("should be the same identifier", ZonedDateTime.of(2018,01,01,00,00,00,0,ZoneOffset.UTC), new TimeStamp(ZonedDateTime.of(2018,01,01,00,00,00,0,ZoneOffset.UTC)).getInstant());
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }
}
