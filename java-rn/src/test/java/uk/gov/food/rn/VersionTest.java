/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
package uk.gov.food.rn;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests on {@link Version}
 */
public class VersionTest {

    @Test
    public void shouldAllowLegalVersionId() {
        assertTrue("should be a legal identifier", Version.isValidIdentifier(0));
        assertTrue("should be a legal identifier", Version.isValidIdentifier(5));
        assertTrue("should be a legal identifier", Version.isValidIdentifier(9));
    }

    @Test
    public void shouldNotAllowIllegalVersionId() {
        assertFalse("should not be a legal identifier", Version.isValidIdentifier(-1));
        assertFalse("should not be a legal identifier", Version.isValidIdentifier(10));
    }

    @Test
    public void shouldConstructALegalVersion() {
        try {
            assertNotNull(new Version(1));
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }

    @Test
    public void shouldNotConstructALegalVersion() {
        try {
            assertNotNull(new Version(10));
            fail("should not be permitted");
        }
        catch (RNException e) {
        }
    }

    @Test
    public void shouldDecodeACorrectlyEncodedVersion() {
        try {
            assertNotNull(new Version("4"));
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }

    @Test
    public void shouldNotConstructIncorrectlyEncodedVersion() {
        try {
            assertNotNull(new Version("20"));
            fail("should not be permitted");
        }
        catch (RNException e) {
        }
    }

    @Test
    public void shouldReturnTheVersionId() {
        try {
            assertEquals("should be the same identifier", 1, new Version(1).getId());
            assertEquals("should be the same identifier", 2, new Version("2").getId());
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }
}
