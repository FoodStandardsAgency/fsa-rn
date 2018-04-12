/* Copyright (c) 2018 Crown Copyright (Food Standards Agency) */

package uk.gov.food.rn;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import uk.gov.food.rn.Authority;
import uk.gov.food.rn.Instance;
import uk.gov.food.rn.RN;
import uk.gov.food.rn.Type;

/**
 * Unit tests for {@link RN}.
 */
public class RNTest
{
    @Test
    public void itShouldConstructAnRNFromElements() {
        RN rn = new RN(new Authority(1234), new Instance(5), new Type(6), timestampFixture());

        assertEquals(1234, rn.getAuthority().getId());
        assertEquals(5, rn.getInstance().getId());
        assertEquals(6, rn.getType().getId());
        assertEquals(timestampFixture(), rn.getInstant().getInstant());
    }

    @Test
    public void itShouldConstructAnRNFromAnInternalNumber() {
        RN rn = new RN(internalRepresentationFixture());

        assertEquals(1234, rn.getAuthority().getId());
        assertEquals(5, rn.getInstance().getId());
        assertEquals(6, rn.getType().getId());
        assertEquals(timestampFixture(), rn.getInstant().getInstant());
    }

    @Test
    public void itShouldConstructAnRNFromAnEncodedNumber() {
        RN rn = new RN("7SQ34N-8221TH-LMDNYG");

        assertEquals(1234, rn.getAuthority().getId());
        assertEquals(5, rn.getInstance().getId());
        assertEquals(6, rn.getType().getId());
        assertEquals(timestampFixture(), rn.getInstant().getInstant());
    }

    @Test
    public void itShouldEncodeAReferenceNumberToExternalForm() {
        RN rn = new RN(new Authority(1234), new Instance(5), new Type(6), timestampFixture());
        assertEquals("7SQ34N-8221TH-LMDNYG", rn.getEncodedForm());
    }

    @Test
    public void itShouldGenerateAHumanReadbleFormForDebugging() {
        RN rn = new RN(new Authority(1234), new Instance(5), new Type(6), timestampFixture());
        assertEquals("1234:5:06:2018-04-12T12:34:51.468Z", rn.toString());
    }

    @Test
    public void itShouldDetectWhenAnExternalFormIsCorrupted() {
        try {
            String rnEnc = "7SQ34N-8221TH-LMDNYG";
            String rnCorrupt = rnEnc.replace('Q', 'S');

            new RN(rnCorrupt);
            fail("Should raise exception");
        }
        catch (RNException e) {
            assertEquals("Value '509906315616374199643526092' does not have intact check digits", e.getMessage());
        }
    }

    @Test
    public void itShouldDetectEquality() {
        RN rn0 = new RN(new Authority(1234), new Instance(5), new Type(6), timestampFixture());
        RN rn1 = new RN(new Authority(1234), new Instance(5), new Type(6), timestampFixture());

        assertEquals(rn0, rn1);
        assertEquals(rn0.hashCode(), rn1.hashCode());
    }

    @Test
    public void itShouldDetectInequality() {
        RN rn0 = new RN(new Authority(1234), new Instance(5), new Type(6), timestampFixture());
        RN rn1 = new RN(new Authority(1243), new Instance(5), new Type(6), timestampFixture());

        assertNotEquals(rn0, rn1);
        assertNotEquals(rn0.hashCode(), rn1.hashCode());
    }

    protected ZonedDateTime timestampFixture() {
        return ZonedDateTime.of(2018, 04, 12, 12, 34, 51, 468*1000000, ZoneOffset.UTC);
    }

    /** Corresponds to 1234:5:6:20180412123451 */
    protected BigInteger internalRepresentationFixture() {
        return new BigInteger("468123450620180412123451");
    }
}
