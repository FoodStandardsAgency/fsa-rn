/******************************************************************
 * File:        Codec.java   
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  10 Apr 2018
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
 *****************************************************************/
package com.epimorphics.fsa.rn;

import java.math.BigInteger;
import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

	/**
	 * @author skw
	 *
	 */
	public class Codec {

	    private static final long serialVersionUID = 2490953697826083512L;

	    private static String           alphabet    = "ABCDEFGHJKLMNPQRSTVWXYZ1234567890";
	    private static final int        i_base      = alphabet.length();                               // 33
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
	        
	        return raw_encode(i);
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
	    
	    public static BigInteger decode(String i_b33) throws RNException {
	    	BigInteger i = raw_decode(i_b33);
	    	if(isValid(i)) {
	    		return i.divide(baseSquared);
	    	}	        
	    	throw new RNException(String.format("Bad checked number \"%s\": digit check failed",i_b33));
	    }

	    /**
	     * Decodes an base 33 encoded number string to a BigInteger.
	     *
	     * @param i_b33 Base 33 encoded string
	     * @return
	     * @throws RNException
	     */
	    private static BigInteger raw_decode(String i_b33) throws RNException {
	        BigInteger res = BigInteger.ZERO;

	        if (!i_b33.matches("["+alphabet+" -]+")) {
	          throw new RNException("Bad Reference Number \""+i_b33+"\" contains character not in alphabet: ["+alphabet+"]");
	        }

	        String digits = i_b33.replaceAll("[ -]+","");
	        for(char digit : digits.toCharArray()) {
	            int digit_value = alphabet.indexOf(digit);
	            // Shift result left on digit and add the value of this new digit
	            res = res.multiply(base).add(BigInteger.valueOf(digit_value) );
	        }
	        return res;
	    }


	    /**
	     * Checks the validity of a BigInteger as an rn string.
	     *
	     * @param i
	     * @return
	     */
	    private static boolean isValid(BigInteger i) {
	        return i.mod(prime).equals(BigInteger.ZERO) ;		 // Checksum

	    }	    
	    
	    public static boolean isValid(String i_b33) {
	    	try {
				return isValid(raw_decode(i_b33));
			} catch (RNException e) {
				// TODO Auto-generated catch block
				return false;
			}
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

	

