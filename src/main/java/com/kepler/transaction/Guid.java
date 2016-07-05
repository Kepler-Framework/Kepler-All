package com.kepler.transaction;

/**
 * GUID
 * 
 * @author KimShen
 *
 */
public class Guid {

	private static final ThreadLocal<String> GUID = new ThreadLocal<String>();

	public static String get() {
		return Guid.GUID.get();
	}

	public static void release() {
		Guid.GUID.set(null);
		Guid.GUID.remove();
	}

	public static void set(String guid) {
		Guid.GUID.set(guid);
	}
}
