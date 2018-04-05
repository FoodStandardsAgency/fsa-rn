/*******************************************************************************
 * File:        RN.java
 * Created by:  skw
 * Created on:  5 Apr 2018
 * 
 * (c) Copyright 2018, Epimorphics Limited
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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

public class RN {
	
	private static final long serialVersionUID = 2490953697826083512L;

	private static String     alphabet = "ABCDEFGHJKLMNPQRSTVWXYZ1234567890";
	private static BigInteger alphaLen = BigInteger.valueOf(alphabet.length()); 
	private static final int  base     = alphabet.length();
	
	// Calculate the largest prime less than the square of our number base (so that we have 2 check digits)
	private static final BigInteger prime  = BigInteger.valueOf(largestPrimeBelow(base*base));
	private static final BigInteger baseSquared = BigInteger.valueOf(base*base);

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
	
	private int     authority  ;
	private int     instance  ;
	private int     type ;
	private ZonedDateTime instant = null ;
	private String     rn_str = null ;
	
	/**
	 * Constructs a new rn form a decimal string
	 * 
	 * @param decimalForm A decimal number in string form to be encoded as an rn
	 * @throws RNException 
	 */
	protected RN(String decimalForm) throws RNException  {
		this(new BigInteger(decimalForm));
	}
	
	protected RN(BigInteger i) throws RNException {
		parseDecimal(i);
		rn_str = encode(i);		
	}
	
	/*
	 * parseDecimal
	 * 
	 * Fast version that uses substring to break apart a BigDecimal.
	 */
	private void parseDecimal(BigInteger decimal) throws RNException {
		String i = String.format("%024d", decimal);

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
		type      = Integer.parseInt(i.substring(8,10));
		instance  = Integer.parseInt(i.substring(7,8));
		authority = Integer.parseInt(i.substring(3,7)); 
		int milli = Integer.parseInt(i.substring(0,3));
				
	
		if( !( i.length()==24 &&
			  0<=milli   && milli<=999 &&
		      0<=sec     && sec<=60    &&
			  0<=min     && min<=59    &&
			  0<=hour    && hour<=23   &&
			  1<=day     && day<=31    &&
			  1<=month   && month<=12  &&
			  2000<=year && year<=9999 &&
			  0<=type    && type<=99 &&
			  0<=instance && instance<=9 &&
			  1000<=authority && authority<=9999 ) ) {
			throw new RNException("Bad Decimal form: "+ i);
		}
		
		try {		
			instant = ZonedDateTime.of(year, month, day, hour, min, sec, milli*1000000, ZoneOffset.UTC);
		} catch (DateTimeException e) {
			throw new RNException("Bad Decimal form (illegal date): "+i,  e);
		}
	}
	
	/*
	 * parseDecimal (2)
	 * 
	 * 
	 */
	private void parseDecimal2(BigInteger decimal) throws RNException {
		BigInteger i = decimal; 
		int sec = i.mod(ONEHUNDRED).intValue();
	    i = i.divide(ONEHUNDRED);
		int min = i.mod(ONEHUNDRED).intValue();
		i = i.divide(ONEHUNDRED);
		int hour = i.mod(ONEHUNDRED).intValue();
		i = i.divide(ONEHUNDRED);
		int day  = i.mod(ONEHUNDRED).intValue();
		i = i.divide(ONEHUNDRED);
		int month = i.mod(ONEHUNDRED).intValue();
		i = i.divide(ONEHUNDRED);
		int year = i.mod(TENTHOUSAND).intValue();
		i = i.divide(TENTHOUSAND);
		
		type = i.mod(ONEHUNDRED).intValue();
		i = i.divide(ONEHUNDRED);
		
		instance = i.mod(BigInteger.TEN).intValue();
		i = i.divide(BigInteger.TEN);
		
		authority = i.mod(TENTHOUSAND).intValue();
		i = i.divide(TENTHOUSAND);
		
		int milli = i.mod(ONETHOUSAND).intValue();
		i = i.divide(ONETHOUSAND);
		
		int residue = i.intValue();
	
		if( !(0<=milli   && milli<=999 &&
		      0<=sec     && sec<=60    &&
			  0<=min     && min<=59    &&
			  0<=hour    && hour<=23   &&
			  0<=day     && day<=31    &&
			  1<=month   && month<=12  &&
			  2000<=year && year<=9999 &&
			  0<=type    && type<=99 &&
			  0<=instance && instance<=9 &&
			  1000<=authority && authority<=9999 &&
			  residue == 0 ) ) {
			throw new RNException("Bad Decimal form: "+ decimal);
		}
				
		try {		
			instant = ZonedDateTime.of(year, month, day, hour, min, sec, milli*1000000, ZoneOffset.UTC);
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
	public String getFieldedForm() {
		return String.format("%04d",getAuthority())+
        ":" + String.format("%01d",getInstance())+
        ":" + String.format("%02d",getType())+
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
			char ch = alphabet.charAt(  (i.mod( alphaLen  )).intValue() );
			i = i.divide(alphaLen);

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
			res = res.multiply(alphaLen);
			
			res = res.add(BigInteger.valueOf(increment) );
		}		
		return res;
	}
	
	
	/**
	 * Checks the validity of an encoded rn string
	 * 
	 * @param rn_s rn string to be checked for validity
	 * @return
	 */
	public static boolean isValid(String rn_s) {
		BigInteger res;
		try {
			res = raw_decode(rn_s);
		} catch (RNException e) {
			return false ;
		}		
		return isValid(res);
	}
	
	
	private static Pattern p = Pattern.compile(
			                      "[0-9]{3}"+                  //Millisec  000-999
			                      "[1-9][0-9]{4}"+             //Authority + serial  1000-9990 + 0-9
			                      "[0-9]{2}"+                  //Type  00-99
	                              "[2-9][0-9]{3}"+             //Year  2000-9999
	                              "((0[1-9])|(1[0-2]))"+       //Month 01-12
	                              "(([0-2][0-9])|(3[0-1]))"+   //Day   01-31
	                              "(([0-1][0-9])|(2[0-3]))"+   //Hour  00-23
	                              "([0-5][0-9])"+              //Min   00-59
	                              "([0-5][0-9])"               //Sec   00-59
	                           ) ;
	
	
	/** 
	 * Checks the validity of a BigInteger as an rn string.
	 * 
	 * @param rn_i
	 * @return
	 */
	private static boolean isValid(BigInteger rn_i) {		
		return rn_i.mod(prime).equals(BigInteger.ZERO)		 // Checksum
			&& p.matcher(String.format("%024d",rn_i.divide(baseSquared))).matches() ;  // Number pattern
	}
	
	/**
	 * @return the authority
	 */
	public int getAuthority() {
		return authority;
	}

	/**
	 * @return the instance
	 */
	public int getInstance() {
		return instance;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the instant
	 */
	public ZonedDateTime getInstant() {
		return instant;
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
	
	public String toString() {
		return rn_str;
	}
}
