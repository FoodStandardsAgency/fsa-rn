/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
package uk.gov.food.rn;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.gov.food.rn.Authority;
import uk.gov.food.rn.RNException;

/**
 * Unit tests on {@link Authority}
 */
public class AuthorityTest {
	
	@Test
	public void equalityTest() {
		try {
			assertTrue("should be identical", new Authority(1001).equals(new Authority(1001)) );
		} catch (RNException e) {
			fail();
		}
	}

    @Test
    public void shouldAllowLegalAuthorityId() {
        assertTrue("should be a legal identifier", Authority.isValidIdentifier(1000));
        assertTrue("should be a legal identifier", Authority.isValidIdentifier(5000));
        assertTrue("should be a legal identifier", Authority.isValidIdentifier(9999));
    }

    @Test
    public void shouldNotAllowIllegalAuthorityId() {
        assertFalse("should not be a legal identifier", Authority.isValidIdentifier(0));
        assertFalse("should not be a legal identifier", Authority.isValidIdentifier(999));
        assertFalse("should not be a legal identifier", Authority.isValidIdentifier(10000));
        assertFalse("should not be a legal identifier", Authority.isValidIdentifier(-1));
    }

    @Test
    public void shouldConstructALegalAuthority() {
        try {
            assertNotNull(new Authority(1000));
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }

    @Test
    public void shouldNotConstructALegalAuthority() {
        try {
            assertNotNull(new Authority(100));
            fail("should not be permitted");
        }
        catch (RNException e) {
        }
    }

    @Test
    public void shouldDecodeACorrectlyEncodedAuthority() {
        try {
            assertNotNull(new Authority("1000"));
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }

    @Test
    public void shouldNotConstructIncorrectlyEncodedAuthority() {
        try {
            assertNotNull(new Authority("100"));
            fail("should not be permitted");
        }
        catch (RNException e) {
        }
    }

    @Test
    public void shouldReturnTheAuthorityId() {
        try {
            assertEquals("should be the same identifier", 1111, new Authority(1111).getId());
            assertEquals("should be the same identifier", 2222, new Authority("2222").getId());
        }
        catch (RNException e) {
            fail("should not raise");
        }
    }
}
