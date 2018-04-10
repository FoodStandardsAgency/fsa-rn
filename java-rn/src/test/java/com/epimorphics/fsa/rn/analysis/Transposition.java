/******************************************************************
 * File:        Transposition.java
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  4 Apr 2018
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
package com.epimorphics.fsa.rn.analysis;

import java.math.BigInteger;
import java.util.ArrayList;

public class Transposition {
	static int i_base  = 33;
	static int i_prime = 1087; 
	static int digits  = 18;
	static BigInteger base  = BigInteger.valueOf(i_base);
	static BigInteger prime = BigInteger.valueOf(i_prime);
	
	static ArrayList<Integer> weights = new ArrayList<Integer>(18);
		
	/**
	 * 
	 */
	public static void main(String[] args) {

		initWeights();	
		independentDoubleTranscriptions();
		singleDedEdeTranspositions();
		multiSingleDigitTransciption();
	}

	/**
	 * 
	 */
	private static void independentDoubleTranscriptions() {
		int trials = 0;
		int undetected = 0;
		int detected = 0;
		
		
		for (int i_delta = 1 - i_base; i_delta < i_base; i_delta++) {
			if (i_delta == 0)
				continue;
			for (int j_delta = 1 - i_base; j_delta < i_base; j_delta++) {
				if (j_delta == 0)
					continue;
				// Increment by the number of ways of making the specific delta combinations.
				int inc = (i_base - Math.abs(i_delta)) * (i_base - Math.abs(j_delta));
				
				for (int i_pos = 0; i_pos < digits - 1; i_pos++)
					for (int j_pos = i_pos + 1; j_pos < digits; j_pos++) {
						int sum = i_delta * weights.get(i_pos) + j_delta * weights.get(j_pos);
						if (BigInteger.valueOf(sum).mod(prime).intValue() == 0)
							undetected += inc;
						else
							detected += inc;
						trials += inc;	
					}
			}
		}
		
		System.out.println("Double transcription errors: "+ detected*100.0/trials +"% detected ("+detected+") "
		                    +undetected*100.0/trials+"% undetected ("+undetected+") of "
				            +trials+" trials."  );
	}

	/**
	 * 
	 */
	private static void initWeights() {
		for(int i=0; i<digits ;i++) {
			weights.add(base.pow(i).mod(prime).intValue());
		}
		
		System.out.println("Weights: "+ weights );
	}

	/**
	 * @param weights
	 */
	private static void singleDedEdeTranspositions() {
		boolean ded_ede = false;
		
		for(int i=1; i<digits-1; i++ ) {
			int sum = weights.get(i+1) - weights.get(i) + weights.get(i-1);
			if (BigInteger.valueOf(sum).mod(prime).intValue() == 0) {
				ded_ede = true;
			}
			//System.out.println("Digit position: "+i+" - "+ sum +":"+BigInteger.valueOf(sum).mod(prime));
		}
		System.out.println((ded_ede ? "Some " : "No ")+"DED->EDE Transpositions undetected");
	}

	/**
	 * 
	 */
	private static void multiSingleDigitTransciption() {
		/* Look for sets of indices that sum to our prime base (1087)
		 * 
		 * For multiple occurences of the same single digit transcription error
		 * 
		 *  (d' - d).sum(Wk) = 0 mod p  where k indicates affected digit position
		 *  
		 *  implies sum(Wk) = 0 mod p
		 *
		 */
		int min_indices = Integer.MAX_VALUE;
		boolean contiguous_indices = false;
		long trials = 0;
		long sets = 0;
		for(long j=1; j< (1<<digits); j++) {
			int sum = 0;
			ArrayList<Integer> indices = new ArrayList<Integer>(digits);
			ArrayList<Integer> values = new ArrayList<Integer>(digits);
			for(int k=0; k<18; k++) {
				if ( (j & (1<<k)) !=0 ) {
					sum += weights.get(k);
					indices.add(k);
					values.add(weights.get(k));
				}
			}
			trials++;
			if (BigInteger.valueOf(sum).mod(prime).intValue() == 0) {
				sets++;
				min_indices = Math.min(min_indices, indices.size());
				boolean local_contiguous = true;
				for(int l=0; l< indices.size()-1; l++) {
					if( indices.get(l+1) != indices.get(l) + 1) {
						local_contiguous = false;
						break;
					}
				}
				contiguous_indices = local_contiguous ? true : contiguous_indices;
				
				System.out.println("Indices found: "+ indices.size()+":"+indices +":"+values);	
			}
		}
		System.out.println("Min size of indices: " + min_indices+ " Contiguous Index Group: "+contiguous_indices);
		System.out.println("Found "+sets+" in "+trials+" trials");
	}
}
