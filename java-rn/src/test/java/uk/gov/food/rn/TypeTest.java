/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
package uk.gov.food.rn;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.gov.food.rn.RNException;
import uk.gov.food.rn.Type;

/**
 * Unit tests on {@link Type}
 */
public class TypeTest {

    @Test
    public void shouldAllowLegalTypeId() {
        assertTrue("should be a legal identifier", Type.isValidIdentifier(0));
        assertTrue("should be a legal identifier", Type.isValidIdentifier(5));
        assertTrue("should be a legal identifier", Type.isValidIdentifier(99));
        assertTrue("should be a legal identifier", Type.isValidIdentifier(549));
    }

    @Test
    public void shouldNotAllowIllegalTypeId() {
        assertFalse("should not be a legal identifier", Type.isValidIdentifier(-1));
        assertFalse("should not be a legal identifier", Type.isValidIdentifier(1000));
    }

    @Test
    public void shouldConstructALegalType() {
        try {
            assertNotNull(new Type(1));
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }

    @Test
    public void shouldNotConstructALegalType() {
        try {
            assertNotNull(new Type(1001));
            fail("should not be permitted");
        }
        catch (RNException e) {
        }
    }

    @Test
    public void shouldDecodeACorrectlyEncodedType() {
        try {
            assertNotNull(new Type("4"));
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }

    @Test
    public void shouldNotConstructIncorrectlyEncodedType() {
        try {
            assertNotNull(new Type("2000"));
            fail("should not be permitted");
        }
        catch (RNException e) {
        }
    }

    @Test
    public void shouldReturnTheTypeId() {
        try {
            assertEquals("should be the same identifier", 1, new Type(1).getId());
            assertEquals("should be the same identifier", 2, new Type("2").getId());
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }
}
