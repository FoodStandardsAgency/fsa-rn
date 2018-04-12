/*******************************************************************************
 * File:        Type.java
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  10 Apr 2018
 *
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 *
 ******************************************************************************/
package uk.gov.food.rn;

/**
 * Encapsulates the concept of a reference number(RN) type.
 * 
 * Competent authorities may need to generate RNs of differing types 
 * for different process. Embedding the RN type within a generated RN 
 * enables automated process that to check that an RN is of the type 
 * required for that process.
 * 
 * An RN type is denoted by a two-digit code corresponding to the type identifier in:
 * http://data.food.gov.uk/codes/registration/identifier-type
 * 
 */
public class Type {
    public static final int MIN_TYPE_ID = 0;
    public static final int MAX_TYPE_ID = 99;

    private int id;

    /**
     * Construct a new Type object with the given identifier.
     *
     * @param typeId The identity of the Type
     * 
     */
    public Type(int typeId) {
        if (!isValidIdentifier(typeId))  {
            throw new RNException(
                String.format("Illegal identifier for type: %d is not in the range %d : %d",
                              typeId, MIN_TYPE_ID, MAX_TYPE_ID)
            );
        }

        this.id = typeId;
    }

    /**
     * Construct a new Type object from a string denoting the type identifier
     *
     * @param typeIdEnc The identity of the Type as a string
     * 
     */
    public Type(String typeIdEnc) {
        this(Integer.parseInt(typeIdEnc));
    }

    /**
     * Check if a given integer can denote a valid Type ID. 
     *
     * @param typeId An integer that may denote an type ID
     * @return True if the typeId is in the range of valid type identifiers
     */
    public static boolean isValidIdentifier(int typeId) {
        return (typeId >= MIN_TYPE_ID && typeId <= MAX_TYPE_ID);
    }

    /**
     * @return The identifier for this type, as an int.
     */
    public int getId() {
        return id;
    }
    
	/**
	 * @param other
	 * @return
	 */
	public boolean equals(Type other) {
		return getId() == other.getId();
	}
	
	@Override
    public int hashCode() {
    	return getId();
    }
}
