package com.kepler.com.caucho.hessian.io;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author KimShen
 *
 */
public class LocalDateTimeDeserializer extends AbstractDeserializer {

	@Override
	public Object readObject(AbstractHessianInput in, Object[] fields) throws IOException {
		String[] fieldNames = (String[]) fields;
		int ref = in.addRef(null);
		long initValue = Long.MIN_VALUE;
		for (int i = 0; i < fieldNames.length; i++) {
			String key = fieldNames[i];
			if (key.equals("value")) {
				initValue = in.readUTCDate();
			} else {
				in.readObject();
			}
		}
		Object value = create(initValue);
		in.setRef(ref, value);
		return value;
	}

	private Object create(long initValue) throws IOException {
		if (initValue == Long.MIN_VALUE) {
			throw new IOException(LocalDateTime.class + " expects name.");
		}
		try {
			return LocalDateTime.ofEpochSecond(new Long(initValue) / 1000, Integer.valueOf(String.valueOf(initValue % 1000)) * 1000, ZoneOffset.of("+8"));
		} catch (Exception e) {
			throw new IOExceptionWrapper(e);
		}
	}

	@Override
	public Class<?> getType() {
		return LocalDateTime.class;
	}

}
