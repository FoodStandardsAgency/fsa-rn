/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
package uk.gov.food.rn;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates the representation of a reference number, which includes
 * the roles of converting a representation to and from serialisations
 * of that number as string of characters in some alphabet.
 */
public class Representation {
    /** The set of characters that are permitted for encoding an RN */
    public static final String ALPHABET = "ABCDEFGHJKLMNPQRSTVWXYZ0123456789";

    /** The numerical base for RNs encoded in the ALPHABET */
    public static final BigInteger BASE = BigInteger.valueOf(ALPHABET.length());

    /** The BASE, squared */
    public static final BigInteger BASE_SQUARED = BASE.multiply(BASE);

    /** The largest prime number that is smaller than BASE^2, used in check digits calculation */
    public static final BigInteger CHECK_DIGITS_PRIME = BigInteger.valueOf(1087);

    /** The difference between BASE^2 and CHECK_DIGITS_PRIME, used to calculate check digits more effciiently */
    public static final BigInteger CHECK_DIGITS_RESIDUAL = BASE_SQUARED.subtract(CHECK_DIGITS_PRIME);

    /** The size of groups of digits in the encoded form */
    public static final int GROUP_SIZE = 6;

    /** The maximum length of an encoded representation */
    private int representationLength;

    /** The reference number (RN) that this class is a facade for */
    private RN rn;

    /** Construct a representation for the given reference number with the default length */
    public Representation(RN rn) {
        this(rn, 18);
    }

    /** Construct a representation of a given length, for the given reference number */
    public Representation(RN rn, int length) {
        this.rn = rn;
        this.representationLength = length;
    }

    /**
     * Construct a representation with an encoded form, which can be reversed to an RN.
     *
     * @param encodedRN A string with the encoded form of a reference number
     * @throws RNException If any validation or integrity checks fail on the encoded value
     **/
    public Representation(String encodedRN) {
        this(encodedRN, 18);
    }

    /**
     * Construct a representation with an encoded form, which can be reversed to an RN.
     *
     * @param encodedRN A string with the encoded form of a reference number
     * @throws RNException If any validation or integrity checks fail on the encoded value
     **/
    public Representation(String encodedRN, int length) {
        representationLength = length;

        String cleaned = encodedRN.replaceAll("[\\s-]", "");
        checkPermittedCharacters(cleaned);
        checkEncodedValueSize(cleaned);

        rn = decodeValue(cleaned);
    }

    /** @return The encapsulated reference number */
    public RN getReferenceNumber() {
        return rn;
    }

    /** @return The length of the encoded representation */
    public int getLength() {
        return representationLength;
    }

    /** @return The encoded form of the RN, including check digits */
    public String getEncodedForm() {
        BigInteger nn = getReferenceNumber().getValue();
        BigInteger cn = withCheckDigits(nn);
        String enc = alphabetEncode(cn);
        return groupDigits(enc, GROUP_SIZE);
    }

    /**
     * Calculate the check-digits for the encapsulated reference number. The goal
     * of the check digits is to make is to so that only some encoded reference
     * numbers represent valid examples of RNs, in order to provide some robustness
     * against transcription or transmission errors.
     *
     * For some value NN we compute check digits cc as follows:
     *
     *  cc = prime - ((base^2 * NN) mod prime)
     *
     *  But
     *  	base^2 = prime + residual
     *
     *  So  (base^2 * NN) mod prime
     *      = ((prime * NN) mod prime) + ((residual * NN) mod prime)
     *      = 0 + ((residual * NN) mod prime)
     *      = (residual * NN) mod prime)
     *
     *  cc = largestPrime - ((residual * NN) mod prime)
     *
     *  checked_NN = (NN * base^2) + cc
     *
     *  Checking checked_NN: Test for (checked_NN mod largestPrime) == 0
     *
     *  Recovering NN: NN = checked_NN / base^2
     */
    protected BigInteger withCheckDigits(BigInteger nn) {
        BigInteger cc = CHECK_DIGITS_PRIME.subtract(nn.multiply(CHECK_DIGITS_RESIDUAL).mod(CHECK_DIGITS_PRIME)) ;

        return nn.multiply(BASE_SQUARED).add(cc);
    }

    /**
     * Encodes a BigInteger value using the character alphabet and number base
     *
     * @return  Base 33 serialised form of the given integer.
     */
    protected String alphabetEncode(BigInteger nn) {
        StringBuffer buf = new StringBuffer();
        BigInteger i = nn;

        do {
            int nextDigitValue = i.mod(BASE).intValue();
            i = i.divide(BASE);

            buf.insert(0, ALPHABET.charAt(nextDigitValue));
        } while (i.compareTo(BigInteger.ZERO) > 0 || buf.length() < getLength());

        return buf.toString();
    }

    /**
     * Group digits together for readability, with "-" as a separator
     *
     * @param digits String of digits to be grouped
     * @param groupSize The size of the group (e.g .6)
     * @return Grouped digits
     */
    protected String groupDigits(String digits, int groupSize) {
        StringBuffer buf = new StringBuffer();
        int group = 0;
        String sep = "";

        do {
            buf.append(sep);

            int groupStart = group * groupSize;
            int groupEnd = Integer.min(groupStart + groupSize, digits.length());
            buf.append(digits.substring(groupStart, groupEnd));

            group++;
            sep = "-";
        } while (group * groupSize < digits.length());

        return buf.toString();
    }

    /**
     * Check that an encoded RN string does not contain any characters
     * outside the permitted alphabet, and raise an exception if so.
     */
    protected void checkPermittedCharacters(String encoded) {
        Pattern p = Pattern.compile(String.format("[^%s]", ALPHABET));
        Matcher m = p.matcher(encoded);

        if (m.find()) {
            throw new RNException(String.format("Illegal character in encoded number: '%s' should not contain '%s'",
                                                 encoded, m.group()));
        }
    }

    /**
     * Check that an encoded value is not too long to be a valid RN
     */
    protected void checkEncodedValueSize(String encoded) {
        if (encoded.length() > getLength()) {
            throw new RNException(String.format("'%s' has too many digits", encoded));
        }
    }

    /**
     * Decode an RN from an encoded string
     */
    protected RN decodeValue(String encodedForm) {
        BigInteger cc = decodeDecimal(encodedForm);
        BigInteger nn = checkCheckDigits(cc);

        return new RN(nn);
    }

    /**
     * Decode a value from the base-N alphabet to decimal
     */
    protected BigInteger decodeDecimal(String encodedForm) {
        BigInteger result = BigInteger.ZERO;

        for(char digit: encodedForm.toCharArray()) {
            int digitValue = ALPHABET.indexOf(digit);
            result = result.multiply(BASE).add(BigInteger.valueOf(digitValue) );
        }

        return result;
    }

    /**
     * Check that the check digits in the encoded number indicate
     * that the value is intact. If it is, remove the check digits
     * and return the original result.
     *
     * @param cc An encoded RN value with check digits
     * @return The value without the check digits
     * @throws RNException if the check digits are not correct
     */
    protected BigInteger checkCheckDigits(BigInteger cc) {
        if (cc.mod(CHECK_DIGITS_PRIME).equals(BigInteger.ZERO)) {
            return cc.divide(BASE_SQUARED);
        }
        else {
            throw new RNException(String.format("Value '%s' does not have intact check digits", groupDigits(alphabetEncode(cc),GROUP_SIZE)));
        }
    }
}
