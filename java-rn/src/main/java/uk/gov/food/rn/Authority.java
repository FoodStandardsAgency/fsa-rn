/*******************************************************************************
 * File:        Authority.java
 * Created by:  Ian Dickinson (ian.dickinson@epimorphics.com)
 * Created on:  10 Apr 2018
 *
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 *
 ******************************************************************************/
package uk.gov.food.rn;

/**
 * Encapsulates the concept of an authority, denoting the Local
 * Authority or other body that is capable of issuing new RN
 * identifiers.
 *
 * The authority is denoted by a four-digit integer code,
 * corresponding to the LA identifier in:
 * http://data.food.gov.uk/codes/registration/authority
 */
public class Authority {
    public static final int MIN_AUTHORITY_ID = 1000;
    public static final int MAX_AUTHORITY_ID = 9999;

    private int id;

    /**
     * Construct a new authority object with the given identifier.
     *
     * @param authorityId The identity of the authority
     *
     */
    public Authority(int authorityId) {
        if (!isValidIdentifier(authorityId))  {
            throw new RNException(
                String.format("Illegal identifier for authority: %d is not in the range %d : %d",
                              authorityId, MIN_AUTHORITY_ID, MAX_AUTHORITY_ID)
            );
        }

        this.id = authorityId;
    }

    /**
     * Construct a new authority object from a string denoting the authority identifier
     *
     * @param authorityIdEnc The identity of the authority as a string
     *
     */
    public Authority(String authorityIdEnc) {
        this(Integer.parseInt(authorityIdEnc));
    }

    /**
     * Check if a given integer can denote a valid authority ID. Note: does not
     * check that this is an actual authority ID, since that would entail a network
     * lookup.
     *
     * @param authorityId An integer that may denote an authority ID
     * @return True if the authorityId is in the range of valid authority identifiers
     */
    public static boolean isValidIdentifier(int authorityId) {
        return (authorityId >= MIN_AUTHORITY_ID && authorityId <= MAX_AUTHORITY_ID);
    }

    /**
     * @return The identifier for this authority, as an int.
     */
    public int getId() {
        return id;
    }
    
    
    /**
     * @param other
     * @return
     */
    public boolean equals(Authority other) {
    	return getId() == other.getId();
    }
    
    @Override
    public int hashCode() {
    	return getId();
    }
}
