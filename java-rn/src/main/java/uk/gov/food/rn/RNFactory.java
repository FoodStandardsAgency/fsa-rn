/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
package uk.gov.food.rn;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RNFactory {
	
	// Add shutdown hook to attempt to clear out any held lock files.
	// They may not delete if obtained by other processes
	{ Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	    @Override
	    public void run() {
	      for (int key : factories.keySet()) {
		    RNFactory factory = factories.get(key);
					
		    if (factory.lockFile != null) {
		      try {
			    factory.lockFile.close();
			    factory.lockFile.delete();
			    factory.lockFile = null;
			  } catch (IOException e) {
			    // Ignore
		  	  }
		    }
		  }
	    }
	  }));
	}
	
	static private String LOCK_DIRECTORY = "/var/fsa-rn/locks";
	
    private final Authority authority  ;
    private final Instance  instance   ;
    private final Type      type       ;
    private long  prev = 0 ;
    
    private LockFile lockFile  = null;

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

	public boolean equals(RNFactory other) {
        return authority.equals(other.authority) &&
               instance.equals(other.instance) &&
               type.equals(other.type);
    }

    @Override
    public int hashCode() {
      return hashKey(authority, instance, type);
    }
    
    private static int hashKey(Authority authority, Instance instance, Type type) {
        return Arrays.hashCode(new Object[] { authority, instance, type });    	
    }

    /**
     *  Use a Map to ensure there is a single factory instance for each authority,instance and type combination
     *  within the same JVM.
     */
    private static HashMap<Integer,RNFactory> factories = new HashMap<Integer,RNFactory>() ;

    
    /**
     * Get or create an RNFactory for generating RNs for a given Authority, Instance and Type.
     * 
     * Return an (the) existing factory if one exists, otherwise attempt to make one, then register and return that.
     *  
     * @param authority
     * @param instance
     * @param type
     * @return
     *
     */
    public static RNFactory getFactory(Authority authority, Instance instance, Type type) {
    	RNFactory res;
    	int hashKey = hashKey(authority, instance, type);
        synchronized (factories) {
            if (factories.containsKey(hashKey)) {
                res = factories.get(hashKey);
            } else {
                res = new RNFactory(authority, instance, type);
                factories.put(hashKey, res);
            }
        }
        return res;
    }

    /**
     * Private constructor use to create RNF factories when required. 
     * 
     * @param authority
     * @param instance
     * @param type
     */
    private RNFactory(Authority authority, Instance instance, Type type) {
        this.authority = authority;
        this.instance  = instance;
        this.type      = type;
        
        // Create an external (to JVM) lock file to avoid duplicate instances on this platform.
        lockFile = new LockFile(LOCK_DIRECTORY, 
        						String.format("factory-%04d-%01d-%02d.lock",
        									   authority.getId(),
        									   instance.getId(),
        									   type.getId()
        										));
        try {
        	boolean locked = lockFile.obtain();
        	if(!locked)
            	throw new RNException(String.format("Can't obtain factory lock file \"%s\". Another instance of the same RN generator is probably running on this platform", lockFile.getName() ));
        } catch (IOException e) {
        	throw new RNException("IOException while trying to obtain factory lock", e);
        }
    }

    /**
     * Generates a fresh {@link RN}, pausing for 1ms if less than 1ms has elasped since the factory
     * generated the immediately previous RN. This is to ensure that generated RN are unque.
     * 
     * At present there is NO mechanism in place to ensure that there at most one instance of factory
     * for a given {@link Authority),{@link Instance},{@link Type} combination on a given hardware platform, 
     * only within the same JVM.
     * 
     * There is a need for some external mechanism to ensure for example that simple command line tools running in
     * shell scripts safely create factories. This may rely on some OS IPC mechanism, or possibly a convention for
     * obtaining and exclusive lock on a file for the life time of the given factory object.
     * 
     * @return a fresh unique {@link RN}
     */
    public RN generateReferenceNumber() {
        // Make sure at least one millisecond has elapse since the last reference number was generated
        long time = 0;
        synchronized(this) {
            time = System.currentTimeMillis();
            while (time <= prev) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // Ignore and go round again if needs be.
                }
                time = System.currentTimeMillis();
            }
            prev = time;
        }


        ZonedDateTime instant = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time),ZoneOffset.UTC);

        try {
            return new RN(authority, instance, type, instant);
        } catch (RNException e) {
            return null;
        }

    }
    
    /** @@TODO Factor this main out into some JUnit tests */

    public static void main(String[] args) {
		// Make factory
		RNFactory rnf = null;
		RNFactory rnf2 = null;

		rnf =  RNFactory.getFactory(new Authority(1000), new Instance(1), new Type(21)) ;
		rnf2 = RNFactory.getFactory(new Authority(1000), new Instance(1), new Type(21)) ;

		if(rnf.equals(rnf2) && rnf != rnf2) {
			System.out.println("RNFs are strangely different");
		}

		// Generate some RN's
		RN [] arr = new RN[100];
		for (int i = 0; i < 99; i++) {
			arr[i] = rnf.generateReferenceNumber();
		}

     	arr[99] = new RN( new BigInteger("999999999999991231235959"));

		// Print them out in various forms.
		for(int i = 0; i<100; i++) {
		   System.out.println( arr[i].getEncodedForm() +
				         " " + arr[i].getValue()+
				         " " + arr[i].toDebugString()
		   );
		}

		System.out.println();

		for(int i=0; i<100; i++) {
			String ef = arr[i].getEncodedForm();

			// Break the encoded form

			ef = ef.replaceAll("K","P");

			RN rn;
			try {
				rn = new RN(ef);
				System.out.println(rn.getEncodedForm()+" "+rn.getValue()+" "+rn.toDebugString());
			} catch (RNException e) {
			    System.out.println(e.getMessage());
			}
		}
    }
}
