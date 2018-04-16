/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
package uk.gov.food.rn;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import uk.gov.food.rn.Representation;

/**
 * Unit tests on the {@link Representation} class
 */
public class RepresentationTest {

    @Test
    public void itShouldProvideAccessToTheRepresentationNumber() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        assertSame(rn, r.getReferenceNumber());
    }

    @Test
    public void itShouldProvideAccessToTheRepresentationLength() {
        RN rn = fixture();
        Representation r = new Representation(rn, 99);

        assertEquals(99, r.getLength());
    }

    @Test
    public void itShouldSetTheDefaultRepresentationLength() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        assertEquals(18, r.getLength());
    }

    @Test
    public void itShouldCalculateTheCheckDigits() {
        RN rn = fixture();
        Representation r = new Representation(rn);
        BigInteger i = r.withCheckDigits(rn.getValue());

        // TODO add more semantically meaningful tests here
        assertTrue(i.compareTo(rn.getValue()) > 0);
    }

    /** Note: this test has been updated due to the choice
     * of alphabet ordering. See https://github.com/FoodStandardsAgency/fsa-rn/issues/5 */
    @Test
    public void itShouldEncodeAValueUsingTheAlpabet() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        assertEquals("AAAAAAAAAAAAAAAAAA", r.alphabetEncode(BigInteger.valueOf(0)));
        assertEquals("AAAAAAAAAAAAAAAAA9", r.alphabetEncode(BigInteger.valueOf(32)));
        assertEquals("AAAAAAAAAAAAAAAABA", r.alphabetEncode(BigInteger.valueOf(33)));
        assertEquals("AAAAAAAAAAAAAAAABB", r.alphabetEncode(BigInteger.valueOf(34)));
    }

    @Test
    public void itShouldGroupDigitsForReadability() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        assertEquals("01234", r.groupDigits("01234", 6));
        assertEquals("012345", r.groupDigits("012345", 6));
        assertEquals("012345-6", r.groupDigits("0123456", 6));
        assertEquals("012345-6789A", r.groupDigits("0123456789A", 6));
        assertEquals("012345-6789AB", r.groupDigits("0123456789AB", 6));
        assertEquals("012345-6789AB-C", r.groupDigits("0123456789ABC", 6));
    }

    /** Note: this test has been updated due to the choice
     * of alphabet ordering. See https://github.com/FoodStandardsAgency/fsa-rn/issues/5 */
    @Test
    public void itShouldEncodeAReferenceNumber() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        assertEquals("E057TK-HRLBQW-0PB99N", r.getEncodedForm());
    }

    @Test
    public void itShouldRejectABadlyEncodedNumber() {
        try {
            new Representation("AI0");
            fail("Should be rejected");
        }
        catch (RNException e) {
            assertEquals("Illegal character in encoded number: 'AI0' should not contain 'I'",
                         e.getMessage());
        }
    }

    /** Note: this test has been updated due to the choice
     * of alphabet ordering. See https://github.com/FoodStandardsAgency/fsa-rn/issues/5 */
    @Test
    public void itShouldDecodeAnEncodedNumberToDecimal() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        assertEquals(BigInteger.valueOf(32), r.decodeDecimal("9"));
        assertEquals(BigInteger.valueOf(0), r.decodeDecimal("A"));
        assertEquals(BigInteger.valueOf(815), r.decodeDecimal("10"));
    }

    @Test
    public void itShouldAllowUnchangedCheckDigits() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        BigInteger nn = BigInteger.valueOf(789);
        BigInteger cc = r.withCheckDigits(nn);

        assertEquals(nn, r.checkCheckDigits(cc));
    }

    @Test
    public void itShouldCatchChangedCheckDigits() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        BigInteger nn = BigInteger.valueOf(789);
        BigInteger cc = r.withCheckDigits(nn);
        BigInteger ccDamaged = cc.add(BigInteger.ONE);

        try {
            r.checkCheckDigits(ccDamaged);
            fail("Should be rejected");
        }
        catch (RNException e) {
            assertEquals("Value 'AAAAAA-AAAAAA-AA07VD' does not have intact check digits", e.getMessage());
        }
    }

    @Test
    public void itShouldRoundTripAValueToEncodedForm() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        String s = r.getEncodedForm();

        Representation r1 = new Representation(s);
        assertEquals(rn, r1.getReferenceNumber());
    }

    /** Note: this test has been updated due to the choice
     * of alphabet ordering. See https://github.com/FoodStandardsAgency/fsa-rn/issues/5 */
    @Test
    public void itShouldCatchDamagedEncodings() {
        RN rn = fixture();
        Representation r = new Representation(rn);

        String s = r.getEncodedForm();
        String sDamaged = s.replaceAll("B", "0");

        try {
            new Representation(sDamaged);
            fail("Should be rejected");
        }
        catch (RNException e) {
            assertEquals("Value 'E057TK-HRL0QW-0P099N' does not have intact check digits", e.getMessage());
        }
    }

    protected RN fixture() {
        RN rn = null;
        long time = 1523540718283L;
        ZonedDateTime instant = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);

        try {
            rn = new RN(new Authority(1234), new Instance(5), new Type(6), instant);
        }
        catch (RNException e) {
            //
        }

        return rn;
    }

}
