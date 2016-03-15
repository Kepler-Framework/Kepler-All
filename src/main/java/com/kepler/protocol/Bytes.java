package com.kepler.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Bytes {

	private byte[] bytes;

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
		StringBuffer sb = new StringBuffer();
		ByteBuffer bw = ByteBuffer.wrap(this.bytes);
		int m;
		while (bw.hasRemaining()) {
			m = bw.getInt();
			sb.append(Integer.toHexString(m));
		}
		return sb.toString();
	}

}
