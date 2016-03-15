package com.kepler.protocol;

import java.util.Arrays;

public class Bytes {

	private static final char[] hexCode = "0123456789ABCDEF".toCharArray();
	
	private byte[] bytes;

	public Bytes() {
		
	}
	
	public Bytes(byte[] bytes) {
		this.setBytes(bytes);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Bytes)) {
			return false;
		}
		Bytes that = (Bytes) obj;
		return Arrays.equals(this.bytes, that.bytes);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.bytes);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	
	@Override
	public String toString() {
		StringBuilder r = new StringBuilder(this.bytes.length * 2);
        for (byte b : this.bytes) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
		return r.toString();
	}

}
