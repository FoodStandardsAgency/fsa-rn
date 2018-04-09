/*******************************************************************************
 * File:        RN.java
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  5 Apr 2018
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
 ******************************************************************************/
package com.epimorphics.fsa.rn;

import java.math.BigInteger;
import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author skw
 *
 */
public class RN {

    private static final long serialVersionUID = 2490953697826083512L;

    private static String           alphabet    = "ABCDEFGHJKLMNPQRSTVWXYZ1234567890";
    private static final int        i_base      = alphabet.length(); // 33
    private static BigInteger       base        = BigInteger.valueOf(i_base);
    private static final BigInteger baseSquared = BigInteger.valueOf(i_base*i_base);               //1089

    // Calculate the largest prime less than the square of our number base (so that we have 2 check digits)
    private static final BigInteger prime  = BigInteger.valueOf(largestPrimeBelow(i_base*i_base)); //1087

    // Compute the difference between the number base squared and the largest prime
    private static final BigInteger residual = baseSquared.subtract(prime);

    /*
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
     *  cc = largestPrime - ((residual*value) mod prime)
     *
     *  checked_NN = (NN * base^2) + cc
     *
     *  Checking checked_NN: Test for (checked_NN mod largestPrime) == 0
     *
     *  Recovering NN: NN = checked_NN / base^2
     *
     */

    private static final BigInteger TENTHOUSAND = BigInteger.valueOf(10000);
    private static final BigInteger ONETHOUSAND = BigInteger.valueOf(1000);
    private static final BigInteger ONEHUNDRED  = BigInteger.valueOf(100);

    private Authority authority;
    private Instance  instance  ;
    private Type      type ;
    private Instant   instant = null ;
    private String    rn_str = null ;


    /**
     * @return the authority
     */
    public Authority getAuthority() {
        return authority;
    }

    /**
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the instant
     */
    public ZonedDateTime getInstant() {
        return instant.getInstant();
    }

    public String toString() {
        return rn_str;
    }

    /**
     * Constructs a new rn from a decimal form integer.
     *
     * @param decimalForm    A decimal form integer
     * @throws RNException
     */

    protected RN(BigInteger i) throws RNException {
        parseDecimalForm(i);
        rn_str = encode(i);
    }


    /**
     * Construct an RN from a String in encoded form.
     * @param encodedForm
     * @throws RNException
     */
    protected RN(String encodedForm) throws RNException  {
        BigInteger res = raw_decode(encodedForm);
        if(!isValid(res))
            throw new RNException("Invalid RN (failed digit check):" + encodedForm);

        res = res.divide(baseSquared);
        parseDecimalForm(res);
        // Re-encode into common form.
        rn_str =  encode(res);
    }

    /*
     * parseDecimalForm
     *
     * Fast version that uses substring to break apart a BigDecimal.
     */
    private void parseDecimalForm(BigInteger decimal) throws RNException {
        String i = String.format("%024d", decimal);
        if( i.length()!=24) {
            throw new RNException("Bad Decimal form: "+ i);
        }

        /*
         *          8       16  20
         *          |       |   |
         *  uuuaaaaittyyyyMMddhhmmss
         *  |  |   |  |   |   |   |
         *  0  3   7  10  14  18  22
         *
         */

        int sec   = Integer.parseInt(i.substring(22,24) );
        int min   = Integer.parseInt(i.substring(20,22));
        int hour  = Integer.parseInt(i.substring(18,20));
        int day   = Integer.parseInt(i.substring(16,18));
        int month = Integer.parseInt(i.substring(14,16));
        int year  = Integer.parseInt(i.substring(10,14));
        type      = new Type(i.substring(8,10));
        instance  = new Instance(i.substring(7,8));
        authority = new Authority(i.substring(3,7));
        int milli = Integer.parseInt(i.substring(0,3));

        try {
            instant = new Instant( ZonedDateTime.of(year, month, day, hour, min, sec, milli*1000000, ZoneOffset.UTC) );
        } catch (DateTimeException e) {
            throw new RNException("Bad Decimal form (illegal date): "+i,  e);
        }
    }

    /**
     * Returns the encoded from of an rn
     *
     * @return The encoded string form of an rn.
     */
    public String getEncodedForm() {
        return rn_str;
    }


    /**
     * Returns the decoded decimal form of an rn as a string of decimal digits.
     *
     * @return The decoded decimal string form of an rn
     */
    public String getDecimalForm() {
        try {
            return String.format("%024d", raw_decode(rn_str).divide(baseSquared));
        } catch (RNException e) {
            // Should never happen
            return null;
        }
    }

    /**
     * Return a fielded string form of the identifier that reveals its components
     *
     *   - authority
     *   - instance
     *   - type
     *   - instant (as a UTC 8601 dateTime string)
     *
     * @return
     */
    public String getDecodedDecimalForm() {
        return String.format("%04d",getAuthority().getId())+
        ":" + String.format("%01d",getInstance().getId())+
        ":" + String.format("%02d",getType().getId())+
        ":" + getInstant();
    }

    /**
     * A static function that encodes a BigInteger into the rn encoded form.
     *
     * @param i	A BigInteger to be encoded
     * @return  A String carrying the rn in encoded form.
     */
    public static String encode(BigInteger i) {
        // cc = prime - ((i*residual) mod prime)
        BigInteger cc = prime.subtract(i.multiply(residual).mod(prime)) ;
        // i = i*base^2 + cc
        i = i.multiply(baseSquared).add(cc);
        StringBuffer res = new StringBuffer(raw_encode(i));

        return res.toString();
    }


    /**
     * Decodes from the string form of an rn to its decimal value.
     *
     * The input rn value is checked for correct check digits and syntactic form
     *
     * @param rn Encoded rn string to be checked.
     * @return
     * @throws RNException
     */
    public static RN decode(String rn)  {
        BigInteger res;
        try {
            res = raw_decode(rn);
//		     return isValid(res) ? String.format("%024d",res.divide(baseSquared)) : null ;
            return isValid(res) ? new RN(res.divide(baseSquared)) : null ;
        } catch (RNException e) {
            return null ;
        }
    }

    public static boolean isValid(String rn) {
        return (decode(rn) != null);
    }

    /**
     * Encodes a BigInteger value using the character alphabet and number base (33) used for rns
     *
     * A '-' character is inserted between each group of 4 characters to improve readability.
     * These characters are ignored when decoding an rn string.
     *
     * @param i BigInteger value to be base 33 encoded.
     * @return
     */
    private static String raw_encode(BigInteger i) {
        StringBuffer res = new StringBuffer();
        int j = 0;

        do {
            char ch = alphabet.charAt(  (i.mod( base  )).intValue() );
            i = i.divide(base);

             res.insert(0,ch);

            if(++j%6 == 0 && j<18 ) {
                res.insert(0, '-');
            }
        } while ( i.compareTo(BigInteger.ZERO) > 0 || j<18);

        return res.toString();
    }

    /**
     * Decodes an base 33 encoded number string to a BigInteger.
     *
     * @param rn_s Base 33 encoded string
     * @return
     * @throws RNException
     */
    private static BigInteger raw_decode(String rn_s) throws RNException {
        BigInteger res = BigInteger.ZERO;

        if (!rn_s.matches("["+alphabet+" -]+"))
                throw new RNException("Bad Reference number \""+rn_s+"\" contains character not in alphabet: ["+alphabet+"]");

        rn_s = rn_s.replaceAll("[ -]+","");
        for(char ch : rn_s.toCharArray()) {
            int increment = alphabet.indexOf(ch);
            res = res.multiply(base);

            res = res.add(BigInteger.valueOf(increment) );
        }
        return res;
    }


    /**
     * Checks the validity of a BigInteger as an rn string.
     *
     * @param rn_i
     * @return
     */
    private static boolean isValid(BigInteger rn_i) {
        return rn_i.mod(prime).equals(BigInteger.ZERO) ;		 // Checksum

    }

    /**
     * Returns the largest prime number below some limit.
     *
     * @param limit The upper limit the that the sought prime is below.
     * @return
     */
    private static int largestPrimeBelow (int limit) {
        List<Integer> primes = sieveOfEratosthenes(limit);
        return primes.get(primes.size()-1);
    }

    /**
     * Generate a list of prime numbers upto and including n.
     *
     * @param n
     * @return
     */
    private static List<Integer> sieveOfEratosthenes(int n) {
        boolean prime[] = new boolean[n + 1];
        Arrays.fill(prime, true);
        for (int p = 2; p * p <= n; p++) {
            if (prime[p]) {
                for (int i = p * 2; i <= n; i += p) {
                    prime[i] = false;
                }
            }
        }
        List<Integer> primeNumbers = new LinkedList<>();
        for (int i = 2; i <= n; i++) {
            if (prime[i]) {
                primeNumbers.add(i);
            }
        }
        return primeNumbers;
    }


}
