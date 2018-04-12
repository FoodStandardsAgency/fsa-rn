/*******************************************************************************
 * File:        Instance.java
 * Created by:  Stuart Williams (skw@epimorphics.com)
 * Created on:  10 Apr 2018
 *
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 *
 ******************************************************************************/
package uk.gov.food.rn;

/**
 * Encapsulates the concept of a deployment instance, denoting 
 * the particular instance of an RN generator service operated a
 * Competent. This allows a single competent authority to operate
 * multiple RN generator (per reference number type). 
 * 
 * This need may arise because:
 * a) multiple independent systems have a need to generate RNs 
 * b) it may be necessary to transition responsibility for generating RNs 
 *    from one system to another, say during an upgrade or a replacement.
 *
 * The deployment instance is denoted by a single decimal digit code.

 */
public class Instance {
    public static final int MIN_INSTANCE_ID = 0;
    public static final int MAX_INSTANCE_ID = 9;

    private int id;

    /**
     * Construct a new instance object with the given identifier.
     *
     * @param instanceId The identity of the instance (relative to an authority).
     * 
     */
    public Instance(int instanceId) {
        if (!isValidIdentifier(instanceId))  {
            throw new RNException(
                String.format("Illegal identifier for instance: %d is not in the range %d : %d",
                              instanceId, MIN_INSTANCE_ID, MAX_INSTANCE_ID)
            );
        }

        this.id = instanceId;
    }

    /**
     * Construct a new instance object from a string denoting the instance identifier
     *
     * @param instanceIdEnc The identity of the instance as a string (relative to an authority).
     * 
     */
    public Instance(String instanceIdEnc) {
        this(Integer.parseInt(instanceIdEnc));
    }

    /**
     * Check if a given integer can denote a valid instance ID. 
     *
     * @param instanceId An integer that may denote an instance ID
     * @return True if the instanceId is in the range of valid instance identifiers
     */
    public static boolean isValidIdentifier(int instanceId) {
        return (instanceId >= MIN_INSTANCE_ID && instanceId <= MAX_INSTANCE_ID);
    }

    /**
     * @return The identifier for this instance, as an int.
     */
    public int getId() {
        return id;
    }
    
    /**
     * @param other
     * @return
     */
    public boolean equals(Instance other) {
    	return getId() == other.getId();
    }
    
    @Override
    public int hashCode() {
    	return getId();
    }
}
