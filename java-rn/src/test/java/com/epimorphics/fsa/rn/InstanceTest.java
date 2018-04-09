package com.epimorphics.fsa.rn;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests on {@link Instance}
 */
public class InstanceTest {

    @Test
    public void shouldAllowLegalInstanceId() {
        assertTrue("should be a legal identifier", Instance.isValidIdentifier(0));
        assertTrue("should be a legal identifier", Instance.isValidIdentifier(5));
        assertTrue("should be a legal identifier", Instance.isValidIdentifier(9));
    }

    @Test
    public void shouldNotAllowIllegalInstanceId() {
        assertFalse("should not be a legal identifier", Instance.isValidIdentifier(-1));
        assertFalse("should not be a legal identifier", Instance.isValidIdentifier(10));
        assertFalse("should not be a legal identifier", Instance.isValidIdentifier(11));
        assertFalse("should not be a legal identifier", Instance.isValidIdentifier(1000));
    }

    @Test
    public void shouldConstructALegalInstance() {
        try {
            assertNotNull(new Instance(1));
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }

    @Test
    public void shouldNotConstructALegalInstance() {
        try {
            assertNotNull(new Instance(100));
            fail("should not be permitted");
        }
        catch (RNException e) {
        }
    }

    @Test
    public void shouldDecodeACorrectlyEncodedInstance() {
        try {
            assertNotNull(new Instance("4"));
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }

    @Test
    public void shouldNotConstructIncorrectlyEncodedInstance() {
        try {
            assertNotNull(new Instance("100"));
            fail("should not be permitted");
        }
        catch (RNException e) {
        }
    }

    @Test
    public void shouldReturnTheInstanceId() {
        try {
            assertEquals("should be the same identifier", 1, new Instance(1).getId());
            assertEquals("should be the same identifier", 2, new Instance("2").getId());
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }
}
