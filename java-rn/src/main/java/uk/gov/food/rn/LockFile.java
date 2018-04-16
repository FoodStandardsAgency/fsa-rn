/*****************************************************************************
 * Copyright (c) 2018 Crown Copyright (Food Standards Agency)
 * See LICENCE
******************************************************************************/
package uk.gov.food.rn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * @author skw
 *
 */
public class LockFile extends File {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private RandomAccessFile raf = null;
	private FileLock lock        = null;
	FileChannel channel          = null;

	public LockFile(File parent, String child) {
		super(parent, child);
	}

	public LockFile(String path) {
		super(path);
	
	}
	
	public LockFile(String parent, String child) {
		super(parent, child);
	}
	
	public LockFile(URI uri) {
		super(uri);
	}
	
	public synchronized boolean lock() throws IOException {
		try {
			if (lock == null) {
				raf = new RandomAccessFile(this, "rws");
				channel = raf.getChannel();
				lock = channel.tryLock();
			} else if (!lock.isValid()) {
				lock = channel.tryLock();
			}
		} catch (OverlappingFileLockException e) {
			close();
			return false;
		}
		if (lock == null) {
			close();
			return false;
		}
		return lock.isValid();
	}
    
	public synchronized boolean release() throws IOException {
		if (lock!=null) {
			lock.release();
			return !lock.isValid();
		}
		return false;
	}
	
	public synchronized void close() throws IOException {
		if(lock!=null) {
			lock.release();
			lock=null;
		}
		if(channel!=null) {
			channel.close();
			channel = null;
		}
		if(raf!=null) {
			raf.close();
			raf = null;
		}
	}
}
