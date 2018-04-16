/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
package uk.gov.food.rn;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;


/**
 * Unit tests on {@link LockFile}
 */
public class LockFileTest {
	
	private static String LOCK_FILENAME  = "test-file.lock" ;
	private static String LOCK_DIRECTORY = "/var/fsa-rn/locks";

	static {
		File ld = new File(LOCK_DIRECTORY);
		if(!ld.exists()) {
			ld.mkdirs();
		}
	}
	
	
	@Test
	public void checkBasicCreateLockUnlockDelete() {
		LockFile lf = new LockFile(LOCK_DIRECTORY, LOCK_FILENAME);
		try {
			boolean locked =lf.lock();
			assertTrue("lockfile should exist", (new File(LOCK_DIRECTORY, LOCK_FILENAME).exists()) );
			assertTrue("should be locked",locked);
			boolean unlocked = lf.release();
			assertTrue("lockfile should still exist", (new File(LOCK_DIRECTORY, LOCK_FILENAME).exists()) );
			assertTrue("should be unlocked",unlocked);
			
			lf.close();
			boolean deleted = lf.delete();
			assertTrue("lockfile should have been deleted", deleted );
			
		} catch (IOException e) {
			fail(String.format("Unexpected IOException : %s %s", e.getMessage()));
		}
	}
	
	@Test
	public void canRelock() {
		LockFile lf = new LockFile(LOCK_DIRECTORY, LOCK_FILENAME);
		//Create the parent directories if necessary

		try {
			boolean locked =lf.lock();
			assertTrue("lockfile should exist", (new File(LOCK_DIRECTORY, LOCK_FILENAME).exists()) );
			assertTrue("should be locked",locked);
			boolean unlocked = lf.release();
			assertTrue("lockfile should still exist", (new File(LOCK_DIRECTORY, LOCK_FILENAME).exists()) );
			assertTrue("should be unlocked",unlocked);

			boolean relocked =lf.lock();
			assertTrue("lockfile should exist", (new File(LOCK_DIRECTORY, LOCK_FILENAME).exists()) );
			assertTrue("should be locked",relocked);
			boolean reunlocked = lf.release();
			assertTrue("should be unlocked",reunlocked);
			
			lf.close();
			boolean deleted = lf.delete();
			assertTrue("lockfile should have been deleted", deleted );
			
		} catch (IOException e) {
			fail(String.format("Unexpected IOException : %s %s", e.getMessage()));
		}
	}
	
	@Test
	public void shouldNotBeAbleToLockTwice() {
		LockFile lf = new LockFile(LOCK_DIRECTORY, LOCK_FILENAME);
		LockFile lf2 = new LockFile(LOCK_DIRECTORY, LOCK_FILENAME);
		
		try {
			boolean locked =lf.lock();
			assertTrue("lockfile should exist", (new File(LOCK_DIRECTORY, LOCK_FILENAME).exists()) );
			assertTrue("should be locked",locked);
			
			boolean locked2 = lf2.lock();
			assertFalse("should be be able to get a second lock",locked2);
			
			boolean unlocked = lf.release();
			assertTrue("lockfile should still exist", (new File(LOCK_DIRECTORY, LOCK_FILENAME).exists()) );
			assertTrue("should be unlocked",unlocked);

			boolean relocked =lf2.lock();
			assertTrue("lockfile should exist", (new File(LOCK_DIRECTORY, LOCK_FILENAME).exists()) );
			assertTrue("should be locked",relocked);

			boolean reunlocked = lf2.release();
			assertTrue("should be unlocked",reunlocked);

			lf.close();
			boolean deleted = lf2.delete();
			assertFalse("lockfile should be deleted", deleted );
			
			lf2.close();
			deleted = lf.delete();
			assertTrue("lockfile should not be deleted", deleted );
			
		} catch (IOException e) {
			fail(String.format("Unexpected IOException : %s", e.getMessage()));
		}
	}
	
}
