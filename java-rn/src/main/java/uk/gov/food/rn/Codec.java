/******************************************************************
 * File:        Codec.java   
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  10 Apr 2018
 * 
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 *
 *****************************************************************/
package uk.gov.food.rn;

import java.math.BigInteger;

	public class Codec {

	    private static final long serialVersionUID = 2490953697826083512L;

	    private static final String       alphabet    = "ABCDEFGHJKLMNPQRSTVWXYZ1234567890";
	    private static final int          i_base      = alphabet.length();                               // 33
	    private static final BigInteger   base        = BigInteger.valueOf(i_base);
	    private static final BigInteger   baseSquared = BigInteger.valueOf(i_base*i_base);               //1089
	    private static final int          MAX_DIGITS  = 18 ;       
	    private static final BigInteger   MAX_INT     = base.pow(MAX_DIGITS);

	    // 1087 is the largest prime below 1089 (base^2)
	    private static final BigInteger prime  = BigInteger.valueOf(1087); 

	    // Compute the difference between the number base squared and the largest prime
	    private static final BigInteger residual = baseSquared.subtract(prime);

	    /**
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
	     * A static function that encodes a BigInteger into base 33 
	     * serialised form with additional check digits.
	     *
	     * @param i	A BigInteger to be encoded
	     * @return  A String carrying the base 33 serialized form.
	     * 
	     */
	    public static String encode(BigInteger i) {
	        // cc = prime - ((i*residual) mod prime)
	        BigInteger cc = prime.subtract(i.multiply(residual).mod(prime)) ;
	        // i = i*base^2 + cc
	        i = i.multiply(baseSquared).add(cc);
	        
	        //Serialise in base 33
	        return raw_encode(i);
	    }

	    /**
	     * Encodes a BigInteger value using the character alphabet and number base (33) used for RNs
	     *
	     * A '-' character is inserted between each group of 4 characters to improve readability.
	     * These characters are ignored when decoding an rn string.
	     *
	     * @param i BigInteger value to be base 33 encoded.
	     * @return  Base 33 serialised form of the given integer.
	     * 
	     */
	    private static String raw_encode(BigInteger i) {
	    	//Range check
	    	if(i.compareTo(BigInteger.ZERO)<0) 
              	throw new RNException(String.format("Negative values no allowed: %d", i));
	        if( i.compareTo(MAX_INT) > 0)
	            throw new RNException(String.format("Numeric overflow: %d too large MAX_INT = %d", i, MAX_INT));
	        
	        StringBuffer res = new StringBuffer();
	        int j = 0;
	        
	        // Build result from least to most significant (base 33 digit).
	        do {
	        	// Retrieve next digit
	            char ch = alphabet.charAt( (i.mod( base  )).intValue() );
	            // Shift right one place ready for the next digit
	            i = i.divide(base);
	            // Insert this digit as the next most significant digit
	            res.insert(0,ch);

	            //Add some '-' for readability
	            if(++j%6 == 0 && j<MAX_DIGITS ) {
	                res.insert(0, '-');
	            }
	        } while ( i.compareTo(BigInteger.ZERO) > 0 || j<MAX_DIGITS);
        	
	        return res.toString();
	    }

		/**
		 * Check the validity of the check digits on the inbound base 33 encoded value
		 * If valid, removes the check digits and returns the originally encoded integer value.
		 * 
		 * Throws an RNException if the digit check fails.
		 * 
		 * @param i_b33
		 * @return
		 * 
		 */
		public static BigInteger decode(String i_b33) {
	    	BigInteger i = raw_decode(i_b33);
	    	if(isValid(i)) {
	    		return i.divide(baseSquared);
	    	}	        
	    	throw new RNException(String.format("Bad checked number \"%s\": digit check failed",i_b33));
	    }

	    /**
	     * Decodes an base 33 encoded number string to an integer.
	     * 
	     * Throws and RNException if the inbound string is mal-formed.
	     *
	     * @param i_b33 Base 33 encoded string
	     * @return  
	     * 
	     */
	    private static BigInteger raw_decode(String i_b33)  {
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
	     * Checks the validity of check digits in integer form (mod 1087) 
	     *
	     * @param i
	     * @return
	     */
	    private static boolean isValid(BigInteger i) {
	        return i.mod(prime).equals(BigInteger.ZERO) ;		 // Checksum

	    }	    
	    
	    /**
	     * Checks the validity of a Base 33 encoded number with (mod 1087) check digits.
	     * 
	     * @param i_b33
	     * @return
	     */
	    public static boolean isValid(String i_b33) {
	    	try {
				return isValid(raw_decode(i_b33));
			} catch (RNException e) {
				// TODO Auto-generated catch block
				return false;
			}
	    }
	}

	

